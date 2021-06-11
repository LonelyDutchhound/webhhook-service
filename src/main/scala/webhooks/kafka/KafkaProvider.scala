package com.LonelyDutchhound
package webhooks.kafka

import webhooks.config.AppConfig
import webhooks.config.GlobalCfg.HasConfig

import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zio.blocking.Blocking
import zio.duration.durationInt
import zio.kafka.consumer.{CommittableRecord, Consumer, ConsumerSettings, Subscription}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.logging.Logging
import zio._
import zio.clock.Clock

object KafkaProvider {
  type HasKafkaProvider = Has[Kafka]

  case class KafkaConfig(
                          url: String,
                          groupId: String,
                          clientId: String,
                          closeTimeout: Int
                        )

  case class Kafka(kafkaCfg: KafkaConfig) {

    val consumerSettings: ConsumerSettings =
      ConsumerSettings(List(kafkaCfg.url))
        .withGroupId(kafkaCfg.groupId)
        .withClientId(kafkaCfg.clientId)
    val producerSettings: ProducerSettings = ProducerSettings(List(kafkaCfg.url))

    val consumerManaged: ZManaged[Clock with Blocking, Throwable, Consumer.Service] =
      Consumer.make(consumerSettings)
    val producerManaged: ZManaged[Any, Throwable, Producer.Service[Nothing, Int, String]] =
      Producer.make(producerSettings, Serde.int, Serde.string)

    val consumerAndProducer: ZLayer[Clock with Blocking, Throwable, Consumer] =
      ZLayer.fromManaged(consumerManaged) ++ ZLayer.fromManaged(producerManaged)

    val produceMsg: RIO[Blocking with Producer[Any, Int, String], RecordMetadata] = {
      val key: Int = 1
      val value: String = "Webhook Trigger"

      val record = new ProducerRecord("topic-webhook", key, value)

      Producer.produce[Any, Int, String](record)
    }

    val schedulePolicy: Schedule[Any, Any, Long] = Schedule.spaced(3000.milliseconds)

    val scheduledProducer: ZIO[Blocking with Producer[Any, Int, String] with Clock, Throwable, Long] =
      produceMsg.repeat(schedulePolicy)

    def subscribe(
                   handler: CommittableRecord[String, String] => ZIO[Logging, Throwable, Any]
                 ): ZIO[Logging with Blocking with Producer[Any, Int, String] with Clock with Consumer, Throwable, Unit] =
      Consumer.subscribeAnd(Subscription.topics("topic-webhook"))
        .plainStream(Serde.string, Serde.string)
        .tap(_ => scheduledProducer.run)
        .tap(cr => handler(cr))
        .map(_.offset)
        .aggregateAsync(Consumer.offsetBatches)
        .mapM(_.commit)
        .runDrain
  }

  val live: ZLayer[HasConfig, Nothing, HasKafkaProvider] = ZLayer.fromManaged(
    for {
      kafkaCfg <- AppConfig.get.map(_.kafkaConfig).toManaged_
    } yield Kafka(kafkaCfg)
  )

}