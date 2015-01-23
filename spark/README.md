# Install and run



[Sbt](http://www.scala-sbt.org/) is a simple build tool for scala projects. To see targets run `sbt tasks`.
But first you probably need to install it on your machine:
  `brew install sbt`

To test the classes in the spark application:

build a jar with `sbt package` and start the Spark shell.
  `$ bin/spark-shell --jars target/scala-2.10/medusa_2.10-1.0.jar`

and import the clas you need, for example Dictionary
  `scala> import com.foobar.medusa.Dictionary`

This workflow is not fast enough for testing, for that a testing lib will be needed.

# Run.....

```
# prepare
./tools/from_csv.py channel_feed.csv > data/somename.txt

# do it!
sbt build
spark-submit --class com.foobar.medusa.Main --deploy-mode client target/scala-2.10/medusa_2.10-1.0.jar --driver-memory 5g
```

## Get Data

Download a `channel_feed.csv` from _s3_ (google shopping). A good quality dataset should contain:

*   `description`
*   `text`
*   `g:gtin`
*   `g:google_category`

If all fields contain values it means that the fields are mapped correctly. Others are not usable

To process such feed download it to the data directory and run:
  `./tools/from_csv.py channel_feed.csv > mydata.txt

The Main package will pick any files present in the data directory

## Stemming

for Stemming (http://snowball.tartarus.org/texts/introduction.html)
A paper that comes with the package Snowball says:

> Rijsbergen (1979, Chapter 2) assumes document text analysis: stopwords are removed, the remaining words are stemmed, and the resulting set of stemmed word constitute the IR index (and this style of use is widespread today).

This is done in the `from_csv.py`.. but in production we can use Java or a differnt C interface / wrapper.
For prototype purposes this works fast enough.

