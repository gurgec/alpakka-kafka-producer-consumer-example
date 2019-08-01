name := "alpakka-kafka-producer-consumer-example"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "1.1.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"

libraryDependencies += "com.github.danielwegener" % "logback-kafka-appender" % "0.2.0-RC2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
