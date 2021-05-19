package com.LonelyDutchhound
package webhooks.service.services.webhook

import webhooks.AppEnv
import webhooks.service.services.webhook.Models.{Webhook}
import webhooks.service.{ApiService, ApiServiceEffect, RestService, ServiceInfo}

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, ZIO}

object WebhookService extends RestService[AppEnv]{

  override def info: ServiceInfo = ServiceInfo("webhooks", 1.0, "Сервис по работе с webhooks")

  override def routes: ApiServiceEffect[HttpRoutes[Task]] = routes_

  override protected val services: List[ZServerEndpoint[CoreEnv, _, ApiService.ErrorResponse, _]] = List(
    rootPath
      .get.description("get all webhooks")
      .name("getAllWebhooks")
      .in("webhooks")
      .out(jsonBody[Seq[Webhook]].description("webhooks data"))
      .logic(
        _ => Logics.getAllWebhooks()
      ),

//    rootPath
//      .post.description("add new webhook")
//      .name("addhook")
//      .in("webhooks")
//      .in(jsonBody[Webhook].description("webhook creation data"))
//      .out(jsonBody[Webhook].description("webhook"))
//      .logic(
//        (_, webhook) => Logics.saveWebhook(webhook)
//      )
  )
}
