import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object StackOverflowTests {
  def testKmeans1(sc: SparkContext): Unit = {
    val means = Array((1, 1), (-1, -1))
    val points = sc.makeRDD(List((1, 1), (-1, -1)))

    val result = StackOverflowAnalysis.StackOverflow.kmeans(means, points, debug = true)

  }

  def testKmeans2(sc: SparkContext): Unit = {
    val means = Array((1, 1), (-1, -1))
    val points = sc.makeRDD(List(
                              (1, 1),
                              (-1, -1),
                              (2, 2),
                              (3, 3),
                              (-2, -2),
                              (-3, -3)
                            ))

    val result = StackOverflowAnalysis.StackOverflow.kmeans(means, points, debug = true)
  }

  def testKmeans3(sc: SparkContext): Unit = {
    val means = Array((1, 1), (-1, -1))
    val points = sc.makeRDD(List(
                              (1, 1),
                              (-1, -1),
                              (2, 2),
                              (3, 3),
                              (-2, -2),
                              (-3, -3)
                            ))

    val result = StackOverflowAnalysis.StackOverflow.kmeans(means, points, debug = true)

}
