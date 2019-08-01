
import akka.actor.ActorSystem
import kafka.{KafkaConsumer, KafkaProducer}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("alpakka-kafka-actor")

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  KafkaProducer.runProducer.onComplete {
    case Failure(exception) => throw exception
    case _: Success[_] =>
      KafkaConsumer.runConsumer
  }

}
