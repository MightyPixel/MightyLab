import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame

import org.apache.spark.sql.functions.first
  // import org.apache.spark.sql.functions._

// https://github.com/databricks/spark-csv#features
val data = spark.read.format("csv").
  option("header", "true").
  option("nullValue", "?").
  option("inferSchema", "true"). // Needs 2 passes over the data to infer types. Alternative: org.apache.spark.sql.types.StructType
  load("./data/donation/block*.csv").
  cache()

val summary = data.describe()

val is_match_hist = data.rdd.map(_.getAs[Boolean]("is_match")).countByValue

println("True/False = " + is_match_hist(true) + "/" + is_match_hist(false))
println(f"True/False = ${is_match_hist(true) / is_match_hist(false).toDouble}%.4f")


// Alternative way to get the count:
// Using countByValue works works on small number of distinct values
// Also countByValue is action and if we want to use the results we have to resend them to the cluster
data.groupBy("is_match").
  count.
  orderBy($"count".desc). // $ returns a Column from string
  show

data.agg(avg("cmp_fname_c1"), stddev("cmp_fname_c1")).show

// Spark sql alternative
// DataFrames
data.createOrReplaceTempView("linkage")
spark.sql("""
SELECT is_match, count(*) as c
FROM linkage
GROUP BY is_match
ORDER BY c DESC
""").show


// Looking for correlation with match/missing and the rest of the columns
val matches = data.where("is_match = true") // The string arg is as if placed in WHERE SQL clause. Spark SQL needs to parse this. Not recommended
// val matches = data.filter("is_match = true") // where is an alias for the filter.
val misses = data.filter($"is_match" === false) // Good for complex queries. === is overriten by Spark in the Column class
val (matchSummary, missSummary) = (matches.describe(), misses.describe())

// Transposing Dataframes
// in order to better see the difference between the miss and match we want to transform the data as follows:
// Columns: metric, field, value

def pivotSummary(summary: DataFrame): DataFrame = {
  val schema = summary.schema

  // Dataset[(String, String, Double)] => DataFrame
  import summary.sparkSession.implicits._

  val transposedSummary: DataFrame =
    summary.flatMap(row => {
                      val metricName = row.getString(0)
                      val result = (1 until row.size).map(i => {
                                                            (metricName, schema(i).name, row.getString(i).toDouble)
                                                          })
                      result
                    }).toDF("metric", "field", "value")



  val wideSummary = transposedSummary.
    groupBy("field").
    pivot("metric", List("count", "min", "max", "mean", "stddev")).
    agg(first("value"))

  wideSummary
}

val matchSummaryT = pivotSummary(matchSummary)
val missSummaryT = pivotSummary(missSummary)

matchSummaryT.createOrReplaceTempView("match_summary")
missSummaryT.createOrReplaceTempView("miss_summary")

spark.sql("""
SELECT match.field, match.count + miss.count total, match.mean - miss.mean delta_mean, match.stddev - miss.stddev delta_stddev
FROM match_summary match JOIN miss_summary miss ON match.field = miss.field
WHERE match.field NOT IN ("id_1", "id_2")
ORDER BY delta_mean DESC, delta_stddev
""").show()

// Results
// +------------+---------+--------------------+--------------------+
//   |       field|    total|          delta_mean|        delta_stddev|
//   +------------+---------+--------------------+--------------------+
//   |     cmp_plz|5736289.0|  0.9563812499852176|   0.154458653565694|
//   |cmp_lname_c2|   2464.0|  0.8064147192926266|-0.03957085894898113|
//   |      cmp_by|5748337.0|  0.7762059675300512|-0.35209627814698713|
//   |      cmp_bd|5748337.0|   0.775442311783404| -0.3614369509577934|
//   |cmp_lname_c1|5749132.0|  0.6838772482594513|-0.28916240552332345|
//   |      cmp_bm|5748337.0|  0.5109496938298685| -0.4545447665972159|
//   |cmp_fname_c1|5748125.0|  0.2854529057459947|-0.35257392512151875|
//   |cmp_fname_c2| 103698.0| 0.09104268062280174| -0.1902011656639498|
//   |     cmp_sex|5749132.0|0.032408185250332844| -0.0955441826800094|
//   +------------+---------+--------------------+--------------------+
// Food feature is the one that it's values differ significantly for the different classes - in binary classification possitive and negative
// In this case: cmp_lname_c2 and cmp_lname_c2 have too little values so we can not rely on them. cmp_sex have very close means.
// Good features are: cmp_plz, cmp_by and cmp_bd



