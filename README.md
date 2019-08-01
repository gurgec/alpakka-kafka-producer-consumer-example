# Alpakka Kafka Producer and Consumer Example

Alpakka Kafka Producer and Consumer is an example of how we can use Akka streams with Apache Kafka.

The implementation consists of two parts. 
## Producer part: 
This part reads a csv file downloaded from kaggle. The data consists of goodreads book data with various language. Via producer flow each line is csv converted to json and send to Apache Kafka topic.
## Consumer part: 
Consumes Apache Kafka messages and via Filter Flow filters language and export to language file.

## Dependencies and Prerequisites

* Scala Version: 2.13.0
* Bootstrap servers of the Kafka cluster
* Dependencies:
   * ```"com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4" ```
   * ```"com.lightbend.akka" %% "akka-stream-alpakka-csv" % "1.1.0"```
   * ```"com.lightbend.akka" %% "akka-stream-alpakka-file" % "1.1.0"```

## Usage

```> sbt run```

## Contributing
This is an example project. 

## License
[MIT](https://choosealicense.com/licenses/mit/)