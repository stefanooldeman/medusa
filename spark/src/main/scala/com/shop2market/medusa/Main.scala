package com.foobar.medusa

import org.apache.spark.SparkContext._
// important! the above import ensures implicit type conversions. 
import org.apache.spark.{SparkContext, SparkConf}

object Main extends App {

   val conf = new SparkConf().setAppName("medusa")
                             .setMaster("local[2]")
                             .set("spark.executor.memory", "4g")
  val sc = new SparkContext(conf)

  val rawblocks = sc.textFile("data")
  val categories = new CategoryVectors(rawblocks)
  categories.trainAndStore()

}
