organization := "com.foobar"
name := "medusa"

version := "1.0"

scalaVersion := "2.10.4"

val sparkVersion = "1.2.0"

libraryDependencies <<= scalaVersion {
  scala_version => Seq(
    // Spark and Mllib
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    // Lucene
    "org.apache.lucene" % "lucene-core" % "4.8.1",
    // for Porter Stemmer
    "org.apache.lucene" % "lucene-analyzers-common" % "4.8.1",
    // Guava for the dictionary
    "com.google.guava" % "guava" % "18.0"
  )
}

// for examples, look at the github repos of these packages: http://spark-packages.org/
