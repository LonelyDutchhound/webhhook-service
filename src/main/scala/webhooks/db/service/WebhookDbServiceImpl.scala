package com.LonelyDutchhound
package webhooks.db.service

import webhooks.db._
import webhooks.db.model.WebhookModelDb
import webhooks.db.model.WebhookModelDb.Materialize
import webhooks.db.service.WebhookDbService.DbService
import webhooks.service.ApiService.BadRequest
import webhooks.service.services.webhook.Models.Webhook

import cats._
import cats.data._
import cats.effect._
import cats.free.Free
import cats.implicits._
import doobie._
import doobie.free.connection
import doobie.implicits._
import doobie.postgres._
import doobie.implicits.javatime._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import zio.logging.Logging
import zio.{RIO, Task, ZIO}

import java.util.UUID

object WebhookDbServiceImpl {
  def apply(db: DbConnect.Service): WebhookDbServiceImpl = new WebhookDbServiceImpl(db)
}

class WebhookDbServiceImpl (db: DbConnect.Service) extends DbService {

  private def noSuchWebhook(id: UUID) = BadRequest(s"webhook(id=$id) does not exists")

  override def getAllWebhooks: ZIO[Logging, Throwable, List[Webhook]] = for {
    ret <- db.executeQuery(WebhookModelDb.select.query[WebhookModelDb.Materialize].to[List])
    res = ret.map(t => Webhook(
      Some(t.system.id),
      t.data.name,
      t.data.url
    ))
  } yield res

  override def saveWebhook(webhook: Webhook): RIO[Logging, Webhook] = for {
    id <- db.executeQuery(WebhookModelDb.insert(
      WebhookModelDb(
                       webhook.name,
                       webhook.url
                     )).withUniqueGeneratedKeys[UUID]("id"))
    newWebhook <- getWebhookById(id)
  } yield newWebhook


  private def getWebhookById(uuid: UUID): RIO[Logging, Webhook] = for {
    ret <- db.select[WebhookModelDb.Materialize](WebhookModelDb.selectById(uuid))
    _ <- ZIO.fail(noSuchWebhook(uuid)).when(ret.isEmpty)
    res = webhookDbToRestWebhook(ret)
  } yield res

  private def webhookDbToRestWebhook(webhookFull: List[WebhookModelDb.Materialize]): Webhook = {
    val webhook = webhookFull.head
    val id = webhook.system.id
    val name = webhook.data.name
    val url = webhook.data.url
    Webhook(
              Some(id),
              name,
              url
            )
  }

  override def updateWebhook(id: UUID, webhook: Webhook): RIO[Logging, Webhook] = ???

  override def deleteWebhook(id: UUID): Task[UUID] =
    db.executeQuery(WebhookModelDb.delete(id).update.withUniqueGeneratedKeys[UUID]("id"))

}
