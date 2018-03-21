
// https://github.com/databricks/spark-csv#features
val data = spark.read.format("csv")
  .option("header", "true")
  .option("nullValue", "?")
  .option("inferSchema", "true") // Needs 2 passes over the data to infer types. Alternative: org.apache.spark.sql.types.StructType
  .load("./data/donation/block*.csv")
  .cache()

val summary = data.describe()

val is_match_hist = data.rdd.map(_.getAs[Boolean]("is_match")).countByValue

println("True/False = " + is_match_hist(true) + "/" + is_match_hist(false))
println(f"True/False = ${is_match_hist(true) / is_match_hist(false).toDouble}%.4f")


// Alternative way to get the count:
// Using countByValue works works on small number of distinct values
// Also countByValue is action and if we want to use the results we have to resend them to the cluster
data.groupBy("is_match")
  .count
  .orderBy($"count".desc) // $ returns a Column from string
  .show

data.agg(avg("cmp_fname_c1"), stddev("cmp_fname_c1")).show

// Spark sql alternative
data.createOrReplaceTempView("linkage")
spark.sql("""
SELECT is_match, count(*) as c
FROM linkage
GROUP BY is_match
ORDER BY c DESC
""").show


// Looking for correlation with match/missing and the rest of the columns
val matches = data.where("is_match = true") // The string arg is as if placed in WHERE SQL clause
// val matches = data.filter("is_match = true") // where is an alias for the filter.
val misses = data.filter($"is_match" === false) // Good for complex queries. === is overriten by Spark in the Column class

val (matchSummary, missSummary) = (matches.describe(), misses.describe())

