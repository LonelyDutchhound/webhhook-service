package com.LonelyDutchhound
package webhooks

import webhooks.service.services.webhook.WebhookService

import sttp.tapir.openapi.{Contact, Info}
import zio.{Has, ZIO}

package object service {

  type HasApiService = Has[ApiService]

  type ApiServiceEffect[A] = ZIO[AppEnv, Throwable, A]

  val globalInfo: Info = Info(
    "WebHooks",
    "1.0",
    Some("API backend"),
    None,
    Some(Contact(Some("LonelyDutchhound"), Some("lonely.dutchhound@gmail.com"), None)),
    None
  )

  val servicesApi: List[RestServiceCore] = List(WebhookService)
}
