package com.foobar.medusa

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}
import scala.collection.mutable

object Main extends App {

   val conf = new SparkConf().setAppName("medusa")
                             .setMaster("local[2]")
                             .set("spark.executor.memory", "4g")

  val sc = new SparkContext(conf)
  println(sc)

  val rawblocks = sc.textFile("data")

  println("start")

  // step 1: transform categorties to Map[String, Double] as index
  val categories = rawblocks.map(_.split('|')(0)).cache
  var (labelMap, _) = categories
    .distinct()
    .collect().foldLeft((Map.empty[String,Double], 0.0)) { (data, elem) =>
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

  println("start training")
  // step 3: Machine learning model
  val model = NaiveBayes.train(training, lambda = 1.0)

  println("Done")

}
