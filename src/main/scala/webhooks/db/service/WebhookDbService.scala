package com.LonelyDutchhound
package webhooks.db.service

import webhooks.db.DbConnect
import webhooks.db.DbConnect.HasDbConnect
import webhooks.service.services.webhook.Models.Webhook

import zio.logging.Logging
import zio.{Has, RIO, ZIO, ZLayer}

import java.util.UUID

object WebhookDbService {

  type  HasWebhookDbService = Has[DbService]

  trait DbService {
    def getAllWebhooks(): RIO[Logging, Seq[Webhook]]

    def saveWebhook(webhook: Webhook): RIO[Logging, Webhook]

    def updateWebhook(id: UUID, webhook: Webhook): RIO[Logging, Webhook]

    def deleteWebhook(id: UUID): RIO[Logging, Boolean]
  }

  val live: ZLayer[HasDbConnect, Nothing, HasWebhookDbService] = ZLayer.fromEffect {
    for {
      db <- ZIO.service[DbConnect.Service]
    } yield WebhookDbServiceImpl(db)
  }
}
