package kafka

import java.nio.file.Paths

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult, Materializer}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import util.JsonParser._

import scala.concurrent.{ExecutionContextExecutor, Future}

object KafkaProducer {

  implicit val actorSystem: ActorSystem = ActorSystem("alpakka-kafka-actor")

  final val bootstrapServer: String = ConfigFactory.load().getString("example.kafka.bootstrap-server")

  final val topic: String = ConfigFactory.load().getString("example.kafka.topic")

  final val booksSource=ConfigFactory.load().getString("example.books.books-source")

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val mat: Materializer = ActorMaterializer()

  private val kafkaProducerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServer)

  def runProducer: Future[Done] ={

    val fileSource: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(booksSource))

    val csvFlow: Source[Map[String, String], Future[IOResult]] = fileSource.via(CsvParsing.lineScanner(delimiter = CsvParsing.SemiColon))
      .via(CsvToMap.toMapAsStrings())

    val jsonFlow: Source[String, Future[IOResult]] = csvFlow.map(toJson).map(_.compactPrint)

    val producerFlow: Source[ProducerRecord[String, String], Future[IOResult]] = jsonFlow.map { item: String =>
      new ProducerRecord[String, String](topic, item,item)
    }

    producerFlow.runWith(Producer.plainSink(kafkaProducerSettings))

  }

}
