package com.foobar.medusa

import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}

// import scala.collection.mutable

/**
 * Usage:
 * 1. transform = prepare(rawdata)
 * 2. model = trainAndStore(training)
 * .. some time later you can make several calls
 * 3. use to_tf_idf
 * 4. predict("Foobar Blababla", model)
 */
class CategoryVectors {


  def prepare(data: RDD[String]): RDD[LabeledPoint] = {
    // step 1: transform categorties to Map[String, Double] as index
    val categories = data.map(_.split('|')(0)).cache
    var (labelMap, _) = categories
      .distinct()
      .collect().foldLeft((Map.empty[String,Double], 0.0)) { (data, elem) =>
        val (acc,n) = data; val i = n+1;
        (acc + (elem -> i), i)
      }

    val labels = categories.map(labelMap(_))

    // step 2: Transform terms
    val terms: RDD[Seq[String]] = data.map(_.split('|')(1).split(' ').toSeq)
    val tfidf = this.to_tf_idf(terms)

    // step 3: Combine
    val trainingSet: RDD[LabeledPoint] = labels.zip(tfidf).map {
      case (label, vector) =>
        LabeledPoint(label, vector)
    }.cache()
    return trainingSet
  }


  def trainAndStore(training: RDD[LabeledPoint]): NaiveBayesModel = {
    NaiveBayes.train(training, lambda = 1.0)
  }


  def to_tf_idf(text: String, sc: SparkContext): RDD[Vector] = {
    // this is a strange overload, servers as a good example
    this.to_tf_idf(sc.parallelize(Array(text.split(' ').toSeq)))
  }


  def to_tf_idf(terms: RDD[Seq[String]]): RDD[Vector] =  {
    val hashingTF = new HashingTF()
    val tf: RDD[Vector] = hashingTF.transform(terms)
    tf.cache()

    val idf = new IDF().fit(tf)
    idf.transform(tf)
  }

  def predict(vectors: RDD[Vector], model: NaiveBayesModel): Array[Double] = {
    vectors.map(v => model.predict(v)).collect()
  }


}
