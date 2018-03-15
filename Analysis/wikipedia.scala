import java.io.File

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext


object WikipediaAnalysis extends Serializable {
  val langs = List("JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
                   "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

  case class WikipediaArticle(title: String, text: String) {
    def mentionsLanguage(lang: String): Boolean = text.split(' ').contains(lang)
  }

  def parse(line: String): WikipediaArticle = {
    val oppeningTitleTagString = "\"<page><title>"
    val closingTitleTagString = "</title><text>"
    val closingTitleIndex = line.indexOf(closingTitleTagString)
    val closingTextTagString = "</text></page>\","

    val title = line.substring(oppeningTitleTagString.length, closingTitleIndex)
    val text  = line.substring(closingTitleIndex + closingTitleTagString.length, line.length - closingTextTagString.length)

    WikipediaArticle(title, text)
  }

  def countOccurances(word: String, rdd: RDD[WikipediaArticle]): Int = {
    val sequenceOp = (u: Int, article: WikipediaArticle) => {
      if (article.mentionsLanguage(word)) u + 1
      else u
    }

    rdd.aggregate(0)(seqOp = sequenceOp, combOp = (a, b) => a + b)
  }

  def rankKeywords(keywords: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] = {
    keywords.map(keyword => (keyword, countOccurances(keyword, rdd)))
      .filter({case(_,c) => c > 0})
      .sortBy({case (_, c) => (-c)})
  }

  def makeIndex(keywords: List[String], rdd: RDD[WikipediaArticle]): RDD[(String, Iterable[WikipediaArticle])] = {
    rdd.flatMap(article => keywords.map(keyword => (keyword, article)))
      .filter((tuple: (String, WikipediaArticle))=> {
                val (keyword, article) = tuple
                article.mentionsLanguage(keyword)
              })
      .groupByKey
  }

  def rankKeywordsUsingIndex(index: RDD[(String, Iterable[WikipediaArticle])]): List[(String, Int)] =
    index.mapValues(articles => articles.size)
      .sortBy({case (_, c) => (-c)})
      .collect.toList

  def rankKeywordsReduceByKey(keywords: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] = {
    rdd.flatMap(article =>
      keywords.map(keyword => (keyword, if (article.mentionsLanguage(keyword)) 1 else 0)))
      .reduceByKey(_+_)
      .sortBy({case(_, c) => (-c)})
      .collect.toList
  }

  def compareRankingMethods(sc: SparkContext): Unit = {
    val wikiRdd: RDD[WikipediaArticle] = sc.textFile("data/wikipedia.dat").map(parse)

    val langsRanked: List[(String, Int)] = timed("Method 1: naive ranking", rankKeywords(langs, wikiRdd))
    println(langsRanked)

    val index = makeIndex(langs, wikiRdd)
    val langsRanked2: List[(String, Int)] = timed("Method 2: inverted index ranking", rankKeywordsUsingIndex(index))
    println(langsRanked2)

    val langsRanked3: List[(String, Int)] = timed("Method 3: reduce by key ranking", rankKeywordsReduceByKey(langs, wikiRdd))
    println(langsRanked3)

    println(timing)
  }

  val timing = new StringBuffer
  def timed[T](label: String, code: => T): T = {
    val start = System.currentTimeMillis()
    val result = code
    val stop = System.currentTimeMillis()
    timing.append(s"Processing $label took ${stop - start} ms.\n")
    result
  }
}

