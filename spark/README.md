# Install and run

[Sbt](http://www.scala-sbt.org/) is a simple build tool for scala projects. To see targets run `sbt tasks`.
But first you probably need to install it on your machine:
  `brew install sbt`


# Run in REPL

```
$ spark-shell --driver-memory 5g --jars target/scala-2.10/medusa_2.10-1.0.jar 2> debug.log
OR if you have a cluster
note the executor-memory is per node
$ spark-shell --master spark://localhost:7077 --executor-memory 4g --jars target/scala-2.10/medusa_2.10-1.0.jar 2> debug.log

scala> 
import com.shop2market.medusa._
val rawblocks = sc.textFile("data")
val categories = new CategoryVectors(rawblocks)

val model = categories.trainAndStore
val bModel = sc.broadcast(model)

val mytext = "polyester synthetisch slijtvast beschik vanaf mat 27 tm 43 kleur wit"
val vectors = categories.to_tf_idf(mytext, sc).cache

categories.predict(vectors, bModel.value)
```

# Run as an App

```
# prepare
./tools/from_csv.py channel_feed.csv > data/somename.txt

# do it!
sbt build
spark-submit --class com.shop2market.medusa.Main --deploy-mode client target/scala-2.10/medusa_2.10-1.0.jar --driver-memory 5g
```

## Get Data

Download a `channel_feed.csv` from _s3_ (google shopping). A good quality dataset should contain:

*   `description`
*   `text`
*   `g:gtin`
*   `g:google_category`

If all fields contain values it means that the fields are mapped correctly. Others are not usable

To process such feed download it to the data directory and run:
  `./tools/from_csv.py channel_feed.csv > mydata.txt`

The Main package will pick any files present in the data directory

## Stemming

for Stemming (http://snowball.tartarus.org/texts/introduction.html)
A paper that comes with the package Snowball says:

> Rijsbergen (1979, Chapter 2) assumes document text analysis: stopwords are removed, the remaining words are stemmed, and the resulting set of stemmed word constitute the IR index (and this style of use is widespread today).

This is done in the `from_csv.py`.. but in production we can use Java or a differnt C interface / wrapper.
For prototype purposes this works fast enough.



# Configurations

Put this in the `$SPARK_HOME/conf/spark-env.sh`

```
#!/usr/bin/env bash
export SPARK_WORKER_CORES=2
export SPARK_WORKER_MEMORY=3g
export SPARK_WORKER_INSTANCES=2
```

To manage a local cluster `$SPARK_HOME/sbin/{start,stop}-all.sh`

