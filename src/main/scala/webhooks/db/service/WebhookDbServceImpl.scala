package com.LonelyDutchhound
package webhooks.db.service

import webhooks.db.DbConnect
import webhooks.db.model.WebhookModelDb
import webhooks.db.service.WebhookDbService.DbService
import webhooks.service.services.webhook.Models.Webhook

import zio.{RIO, ZIO}
import zio.logging.Logging

import java.util.UUID

object WebhookDbServiceImpl {
  def apply(db: DbConnect.Service): WebhookDbServiceImpl = new WebhookDbServiceImpl(db)
}
class WebhookDbServiceImpl (db: DbConnect.Service) extends DbService {

  override def getAllWebhooks(): ZIO[Logging, Throwable, List[Webhook]] = for {
    ret <- db.executeQuery(WebhookModelDb.select.query[WebhookModelDb.Materialize].to[List])
    res = ret.map(t => Webhook(
      Some(t.system.id),
      t.data.name,
      t.data.url
    ))
  } yield res

  override def saveWebhook(webhook: Webhook): RIO[Logging, Webhook] = ???

  override def updateWebhook(id: UUID, webhook: Webhook): RIO[Logging, Webhook] = ???

  override def deleteWebhook(id: UUID): RIO[Logging, Boolean] = ???
}
