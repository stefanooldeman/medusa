
/*
 * labels could also be Set[String] = Set.empty 
 * String "Sportartikelen>Teamsport>Voetbal" becomes Set (Sportartikelen, Teamsport, Voetbal)
 */

/*
case class Document(category: String, text: String)

def parse(line: String) : Document = {
val Array(category, text) = line.split('|')
Document(category, text)
}
val collection = rawblocks.map(parse)
*/

import scala.collection.mutable
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD


//val sc = new SparkContext("local[4]", "medusa")
val rawblocks = sc.textFile("785_terms.txt")

// step 1: transform categorties to Map[String, Double] as index
val categories = rawblocks.map(_.split('|')(0)).cache
var (labelMap, _) = categories.distinct().collect().foldLeft((Map.empty[String,Double], 0.0)) { (data, elem) =>
val (acc,n) = data; val i = n+1;
(acc + (elem -> i), i)
}
val labels = categories.map(labelMap(_))


// step 2: Transform terms
val terms: RDD[Seq[String]] = rawblocks.map(_.split('|')(1).split(' ').toSeq)
val hashingTF = new HashingTF()
val tf: RDD[Vector] = hashingTF.transform(terms)

tf.cache()
val idf = new IDF().fit(tf)
val tfidf: RDD[Vector] = idf.transform(tf)

// step 3: Combine
val data = labels.zip(tfidf).map {case (label, vector) => LabeledPoint(label, vector) }
// OR
//val data = sc.parallelize(labels.zip(tfidf).take(100).map {case (label, vector) => LabeledPoint(label, vector) })
val Array(training, test) = data.randomSplit(Array(0.6, 0.4), seed = 11L)

// step 3: Machine learning model
val model = NaiveBayes.train(training, lambda = 1.0)


// val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
//val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

