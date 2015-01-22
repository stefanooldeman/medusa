package com.foobar.medusa

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import scala.collection.mutable


object Main extends App {

  val sc = new SparkContext("local[4]", "medusa")

  val rawblocks = sc.textFile("data")
  case class LabelDocument(id: Long, text: String, label: Double)

  val categories = rawblocks.map(_.split('|')(0)).cache
  // create Map[String, Double] for the categories.
  var (labelMap, _) = categories.distinct().collect().foldLeft((Map.empty[String,Double], 0.0)) { (data, elem) =>
    val (acc,n) = data; val i = n+1;
    (acc + (elem -> i), i)
  }
  val labels = categories.map(labelMap(_))
  // get all teh text / terms
  val terms = rawblocks.map(_.split('|')(1))
  // combine it
  case class Document(label: Double, text: String)
  val training = labels.zip(terms).map { case (x,y) => Document(x,y) }

}
