package com.LonelyDutchhound
package webhooks.service.services.webhook

import java.util.UUID

object Models {

  case class Webhook(
                       id: Option[UUID],
                       name: String,
                       url: String
                     )
}
