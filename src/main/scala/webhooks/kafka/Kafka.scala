package com.LonelyDutchhound
package webhooks.kafka

import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.{URIO, ZIO, ZLayer, ZManaged}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde

case class KafkaConfig(
                        url: String,
                        groupId: String,
                        clientId: String,
                        closeTimeout: Int
                      )

object Kafka {

  val settings: ConsumerSettings =
    ConsumerSettings(List("localhost:9092"))
      .withGroupId("group")
      .withClientId("client")

  val consumerManaged: ZManaged[Clock with Blocking, Throwable, Consumer.Service] =
    Consumer.make(settings)
  val consumer: ZLayer[Clock with Blocking, Throwable, Consumer] =
    ZLayer.fromManaged(consumerManaged)

  Consumer.subscribeAnd(Subscription.topics("topic-webhook"))
    .plainStream(Serde.string, Serde.string)
    .tap(cr => putStrLn(s"key: ${cr.record.key}, value: ${cr.record.value}"))
    .map(_.offset)
    .aggregateAsync(Consumer.offsetBatches)
    .mapM(_.commit)
    .runDrain
}