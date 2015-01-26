package com.foobar.medusa

import scala.collection.mutable

import org.apache.spark.SparkContext._
// important! the above import ensures implicit type conversions. 
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}

// data types:
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint

/**
 * Usage:
 * 1. transform = prepare(rawdata)
 * 2. model = trainAndStore(training)
 * .. some time later you can make several calls
 * 4. predict(to_tf_idf("Foobar Blababla"), model)
 */
class CategoryVectors(data: RDD[String]) extends Serializable {


  // step 1: transform categorties to Map[String, Double] as index
  val categories = data.map(_.split('|')(0)).cache
  var (labelMap, _) = categories
    .distinct()
    .collect().foldLeft((Map.empty[String,Double], 0.0)) { (data, elem) =>
      val (acc,n) = data; val i = n+1;
      (acc + (elem -> i), i)
    }
  val inversedLabelMap = labelMap.map(_.swap)

  val labels = categories.map(labelMap(_))

  // step 2: Transform terms
  val terms: RDD[Seq[String]] = data.map { x => 
      tokanize(x.split('|')(1))
  }
  val tfidf = to_tf_idf(terms)

  // step 3: Combine
  val trainingSet: RDD[LabeledPoint] = labels.zip(tfidf).map {
    case (label, vector) =>
      LabeledPoint(label, vector)
  }.cache()
  // OR
  //val data = sc.parallelize(labels.zip(tfidf).take(100).map {case (label, vector) => LabeledPoint(label, vector) })


  def trainAndStore(): NaiveBayesModel = {
    // val Array(training, test) = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    NaiveBayes.train(trainingSet, lambda = 1.0)
  }

  def to_tf_idf(terms: RDD[Seq[String]]): RDD[Vector] =  {
    val hashingTF = new HashingTF()
    val tf: RDD[Vector] = hashingTF.transform(terms)
    tf.cache()

    val idf = new IDF().fit(tf)
    return idf.transform(tf)
  }

  def to_tf_idf(text: String, sc: SparkContext): RDD[Vector] = {
    // this is a strange overload, servers as a good example
    this.to_tf_idf(sc.parallelize(Array(text).map(tokanize)))
  }

  def predict(vectors: RDD[Vector], model: NaiveBayesModel): (String,Double) = {
    val Array(label) = vectors.map(v => model.predict(v)).collect()
    (inversedLabelMap(label), label)
  }

  def tokanize(line: String): Seq[String] = {
    // todo remove stopwords
    // todo stemming
    // todo remove special chars
    line.map(_.toLower)
      .split(' ').toSeq
  }

}
