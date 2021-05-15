package com.LonelyDutchhound
package webhooks.service.services.webhook

import webhooks.AppEnv
import webhooks.service.services.webhook.Models.Message
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
      .get.description("hello from webhooks")
      .name("getHello")
      .in("hello")
      .out(jsonBody[Message].description("message"))
      .logic(
        _ => ZIO.effect(Message("hello"))
      )
  )
}
