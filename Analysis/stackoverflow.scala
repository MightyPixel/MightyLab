import annotation.tailrec
import scala.reflect.ClassTag

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD


object StackOverflowAnalysis extends Serializable {
  type Question = Posting
  type Answer = Posting
  type QID = Int
  type HighScore = Int
  type LangIndex = Int


  /** A raw stackoverflow posting, either a question or an answer */
  case class Posting(postingType: Int, id: Int, acceptedAnswer: Option[Int], parentId: Option[QID], score: Int, tags: Option[String]) extends Serializable

  class StackOverflow extends Serializable {
    val langs = List("JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
                     "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

    /** K-means parameter: How "far apart" languages should be for the kmeans algorithm? */
    def langSpread = 50000
    assert(langSpread > 0, "If langSpread is zero we can't recover the language from the input data!")

    /** K-means parameter: Number of clusters */
    def kmeansKernels = 45

    /** K-means parameter: Convergence criteria */
    def kmeansEta: Double = 20.0D

    /** K-means parameter: Maximum iterations */
    def kmeansMaxIterations = 120


    /** Load postings from the given file */
    def rawPostings(lines: RDD[String]): RDD[Posting] =
      lines.map(line => {
                  val arr = line.split(",")
                  Posting(postingType =    arr(0).toInt,
                          id =             arr(1).toInt,
                          acceptedAnswer = if (arr(2) == "") None else Some(arr(2).toInt),
                          parentId =       if (arr(3) == "") None else Some(arr(3).toInt),
                          score =          arr(4).toInt,
                          tags =           if (arr.length >= 6) Some(arr(5).intern()) else None)
                })

    /** Group the questions and answers together */
    def groupedPostings(postings: RDD[Posting]): RDD[(QID, Iterable[(Question, Answer)])] = {
      val questions: RDD[(QID, Question)] = postings.filter(posting => posting.postingType == 1).map(posting => (posting.id, posting))
      val answers: RDD[(QID, Answer)] = postings.filter(posting => posting.postingType == 2).map(posting => (posting.parentId.get, posting))

      val questionsAndAnswers: RDD[(QID, (Question, Answer))] = questions.join(answers)
      questionsAndAnswers.groupByKey
    }

    /** Compute the maximum score for each posting */
    def scoredPostings(grouped: RDD[(QID, Iterable[(Question, Answer)])]): RDD[(Question, HighScore)] = {
      grouped.mapValues(qa => {
                          val maxScoreQA = qa.maxBy({case (_, answer) => answer.score})
                          val maxScore: HighScore = maxScoreQA._2.score
                          (maxScoreQA._1, maxScore)
                        }).values
    }

    /** Compute the vectors for the kmeans */
    def vectorPostings(scored: RDD[(Question, HighScore)]): RDD[(LangIndex, HighScore)] = {
      /** Return optional index of first language that occurs in `tags`. */
      def firstLangInTag(tag: Option[String], ls: List[String]): Option[Int] = {
        if (tag.isEmpty) None
        else if (ls.isEmpty) None
        else if (tag.get == ls.head) Some(0) // index: 0
        else {
          val tmp = firstLangInTag(tag, ls.tail)
          tmp match {
            case None => None
            case Some(i) => Some(i + 1) // index i in ls.tail => index i+1
          }
        }
      }

      scored.map({
                   case (question, highScore) => {
                     val langIndex: LangIndex = firstLangInTag(question.tags, langs).get * langSpread
                     (langIndex, highScore)
                   }
                 }).cache
    }


    /** Sample the vectors */
    def sampleVectors(vectors: RDD[(LangIndex, HighScore)]): Array[(Int, Int)] = {

      assert(kmeansKernels % langs.length == 0, "kmeansKernels should be a multiple of the number of languages studied.")
      val perLang = kmeansKernels / langs.length

      // http://en.wikipedia.org/wiki/Reservoir_sampling
      def reservoirSampling(lang: Int, iter: Iterator[Int], size: Int): Array[Int] = {
        val res = new Array[Int](size)
        val rnd = new util.Random(lang)

        for (i <- 0 until size) {
          assert(iter.hasNext, s"iterator must have at least $size elements")
          res(i) = iter.next
        }

        var i = size.toLong
        while (iter.hasNext) {
          val elt = iter.next
          val j = math.abs(rnd.nextLong) % i
          if (j < size)
            res(j.toInt) = elt
          i += 1
        }

        res
      }

      val res =
        if (langSpread < 500)
          // sample the space regardless of the language
          vectors.takeSample(false, kmeansKernels, 42)
        else
          // sample the space uniformly from each language partition
          vectors.groupByKey.flatMap({
                                       case (lang, vectors) => reservoirSampling(lang, vectors.toIterator, perLang).map((lang, _))
                                     }).collect()

      assert(res.length == kmeansKernels, res.length)
      res
    }

    /** Main kmeans computation */
    @tailrec final def kmeans(means: Array[(Int, Int)], vectors: RDD[(Int, Int)], iter: Int = 1, debug: Boolean = false): Array[(Int, Int)] = {
      val newMeans = means.clone()
      val currentMeans: Array[(Int, (Int, Int))] = vectors.map(point => (findClosest(point, means), point)).groupByKey().mapValues(averageVectors).collect

      currentMeans.foreach({
                             case (i, point) => newMeans.update(i, point)
                           })

      val distance = euclideanDistance(means, newMeans)

      if (debug) {
        println(s"""Iteration: $iter
                 |  * current distance: $distance
                 |  * desired distance: $kmeansEta
                 |  * means:""".stripMargin)
        for (idx <- 0 until kmeansKernels)
          println(f"   ${means(idx).toString}%20s ==> ${newMeans(idx).toString}%20s  " +
                    f"  distance: ${euclideanDistance(means(idx), newMeans(idx))}%8.0f")
      }

      if (converged(distance))
        newMeans
      else if (iter < kmeansMaxIterations)
        kmeans(newMeans, vectors, iter + 1, debug)
      else {
        if (debug) {
          println("Reached max iterations!")
        }
        newMeans
      }
    }

    /** Decide whether the kmeans clustering converged */
    def converged(distance: Double) =
      distance < kmeansEta


    /** Return the euclidean distance between two points */
    def euclideanDistance(v1: (Int, Int), v2: (Int, Int)): Double = {
      val part1 = (v1._1 - v2._1).toDouble * (v1._1 - v2._1)
      val part2 = (v1._2 - v2._2).toDouble * (v1._2 - v2._2)
      part1 + part2
    }

    /** Return the euclidean distance between two points */
    def euclideanDistance(a1: Array[(Int, Int)], a2: Array[(Int, Int)]): Double = {
      assert(a1.length == a2.length)
      var sum = 0d
      var idx = 0
      while(idx < a1.length) {
        sum += euclideanDistance(a1(idx), a2(idx))
        idx += 1
      }
      sum
    }

    /** Return the closest point */
    def findClosest(p: (Int, Int), centers: Array[(Int, Int)]): Int = {
      var bestIndex = 0
      var closest = Double.PositiveInfinity
      for (i <- 0 until centers.length) {
        val tempDist = euclideanDistance(p, centers(i))
        if (tempDist < closest) {
          closest = tempDist
          bestIndex = i
        }
      }
      bestIndex
    }


    /** Average the vectors */
    def averageVectors(ps: Iterable[(Int, Int)]): (Int, Int) = {
      val iter = ps.iterator
      var count = 0
      var comp1: Long = 0
      var comp2: Long = 0
      while (iter.hasNext) {
        val item = iter.next
        comp1 += item._1
        comp2 += item._2
        count += 1
      }
      ((comp1 / count).toInt, (comp2 / count).toInt)
    }


    /** Display results **/
    def clusterResults(means: Array[(Int, Int)], vectors: RDD[(LangIndex, HighScore)]): Array[(String, Double, Int, Int)] = {
      def getMedian(xs: List[Int]): Int = {
        if (xs.size % 2 == 0) {
          (xs((xs.size / 2) - 1) + xs(xs.size / 2)) / 2
        } else {
          xs(xs.size / 2)
        }
      }

      val closest: RDD[(Int, (LangIndex, HighScore))] = vectors.map(p => (findClosest(p, means), p))
      val closestGrouped: RDD[(Int, Iterable[(LangIndex, HighScore)])] = closest.groupByKey()

      val median = closestGrouped.mapValues { vs: Iterable[(LangIndex, HighScore)] =>
        val groupByLang: Map[LangIndex, HighScore] = vs.map(_._1 / langSpread).groupBy(identity).mapValues(_.size)
        val mostCommonLang: (LangIndex, HighScore) = groupByLang.maxBy(_._2)

        val langLabel: String   = langs(mostCommonLang._1) // most common language in the cluster
        val langPercent: Double = (groupByLang(mostCommonLang._1) / vs.size) * 100 // percent of the questions in the most common language
        val clusterSize: Int    = vs.size

        val clusterScores = vs.map(_._2).toList.sorted
        val medianScore: Int    = getMedian(clusterScores)

        (langLabel, langPercent, clusterSize, medianScore)
      }

      median.collect().map(_._2).sortBy(_._4)
    }

    def printResults(results: Array[(String, Double, Int, Int)]): Unit = {
      println("Resulting clusters:")
      println("  Score  Dominant language (%percent)  Questions")
      println("================================================")
      for ((lang, percent, size, score) <- results)
        println(f"${score}%7d  ${lang}%-17s (${percent}%-5.1f%%)      ${size}%7d")
    }

  }

  /** The main class */
  object StackOverflow extends StackOverflow {

    def clusterPostsUsingKMeans(sc: SparkContext): Unit = {
      val lines   = sc.textFile("data/stackoverflow.csv")
      val raw     = timed("Parse postings", rawPostings(lines))
      val grouped = timed("Group postings", groupedPostings(raw))
      val scored  = timed("Score postings", scoredPostings(grouped))

      val vectors = timed("Vector postings", vectorPostings(scored))
      // vectors.take(5).foreach(println)

      // assert(vectors.count() == 2121822, "Incorrect number of vectors: " + vectors.count())

      val means   = timed("Kmeans Clustering", kmeans(sampleVectors(vectors), vectors, debug = true))

      val results = timed("Cluster Results", clusterResults(means, vectors))
      printResults(results)
      println(timing)
    }

    val timing = new StringBuffer
    def timed[T](label: String, code: => T): T = {
      val start = System.currentTimeMillis()
      val result = code
      val stop = System.currentTimeMillis()
      timing.append(s"Processing $label took ${(stop - start) / 1000} s.\n")
      result
    }
  }
}

