package kafka

import java.lang.reflect.Field
import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.stream.scaladsl.{FileIO, Flow, RunnableGraph, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import model.Books.Book
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContextExecutor
import spray.json._
import util.JsonParser._
import java.util

object KafkaConsumer {

  implicit val actorSystem: ActorSystem = ActorSystem("alpakka-kafka-actor")

  final val bootstrapServer: String = ConfigFactory.load().getString("example.kafka.bootstrap-server")

  final val groupId: String = ConfigFactory.load().getString("example.kafka.group-id")

  final val topic: String = ConfigFactory.load().getString("example.kafka.topic")

  final val outputFile=ConfigFactory.load().getString("example.books.output-file")

  final val languageList: util.List[String] =ConfigFactory.load().getStringList("example.books.language-list")


  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val mat: Materializer = ActorMaterializer()

  val kafkaConsumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def runConsumer: Consumer.Control ={

    val kafkaSource: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] = Consumer
      .committableSource(kafkaConsumerSettings, Subscriptions.topics(topic))

    val decodeMessageFlow: Source[(CommittableOffset, Book), Consumer.Control] = kafkaSource.via(decodeValueFlow)

    val commitFlow: Source[Book, Consumer.Control] = decodeMessageFlow.map { case (offset, data) =>
      offset.commitScaladsl()
      data
    }


    val filterFlow: Source[Book, Consumer.Control] = commitFlow.filter(book => languageList.contains(book.languageCode))

    val headerCSVSource: Source[Seq[String], NotUsed] = Source.single(Seq("average_rating", "bookID", "ratings_count", "authors", "text_reviews_count", "title", "# num_pages", "isbn13", "language_code", "isbn"))

    val dataFlow: Source[Seq[String], Consumer.Control] =filterFlow
      .map { book =>
        getFieldValuesAsStringList(book)
      }

    val csvWriteFlow: Source[ByteString, Consumer.Control] = dataFlow.prepend(headerCSVSource).via(CsvFormatting.format(delimiter = CsvFormatting.SemiColon))


    val fileSink: RunnableGraph[Consumer.Control] = csvWriteFlow.to(FileIO.toPath(Paths.get(outputFile)))

    fileSink.run()
  }

  private def decodeValueFlow: Flow[CommittableMessage[String, String], (CommittableOffset, Book), NotUsed] =
    Flow[CommittableMessage[String, String]]
      .map { msg: CommittableMessage[String, String] => {
        val messageValue = msg.record.value
        println(s"message: $msg")
        println(messageValue.parseJson)

        (msg.committableOffset, messageValue.parseJson.convertTo[Book])
      }
      }

  private def getFieldValuesAsStringList(cc: AnyRef): Seq[String] =
    cc.getClass.getDeclaredFields.foldLeft(Seq[AnyRef]()){
      (fieldList: Seq[AnyRef], f: Field) =>
        f.setAccessible(true)
        fieldList :+ f.get(cc)
    }.map { value: AnyRef =>
        value match {
          case Some(x) => x.toString
          case None => ""
          case x => x.toString
        }
      }

}
