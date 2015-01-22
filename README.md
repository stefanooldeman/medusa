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
