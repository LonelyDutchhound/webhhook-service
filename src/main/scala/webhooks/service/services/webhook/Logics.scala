package com.LonelyDutchhound
package webhooks.service.services.webhook

import webhooks.db.service.WebhookDbService
import webhooks.db.service.WebhookDbService.HasWebhookDbService
import webhooks.service.services.webhook.Models.Webhook

import sttp.model.StatusCode
import zio.ZIO
import zio.logging.Logging

import java.util.UUID

object Logics {

  def getAllWebhooks: ZIO[Logging with HasWebhookDbService, Throwable, Seq[Webhook]] = for {
    repository <- ZIO.service[WebhookDbService.DbService]
    newWebhook <- repository.getAllWebhooks
  } yield newWebhook

  def saveWebhook(webhook: Webhook): ZIO[Logging with HasWebhookDbService, Throwable, Webhook] = for {
    repository <- ZIO.service[WebhookDbService.DbService]
    newWebhook <- repository.saveWebhook(webhook)
  } yield newWebhook

  def deleteWebhook(id: UUID): ZIO[Logging with HasWebhookDbService, Throwable, (StatusCode, UUID)] = for {
    repository <- ZIO.service[WebhookDbService.DbService]
    uuid <- repository.deleteWebhook(id)
  } yield (StatusCode.Ok, uuid)
}
