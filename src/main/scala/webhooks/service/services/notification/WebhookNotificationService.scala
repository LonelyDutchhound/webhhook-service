package com.LonelyDutchhound
package webhooks.service.services.notification

import webhooks.httpClient.HttpClient.HasHttpClient
import webhooks.kafka.KafkaProvider.HasKafkaProvider

import zio.{Has, ZLayer}

class WebhookNotificationService {

}

object WebhookNotificationService {

  type HasWebhookNotificationService = Has[WebhookNotificationService]

  val live: ZLayer[HasKafkaProvider with HasHttpClient, Nothing, HasWebhookNotificationService] = ???
}
