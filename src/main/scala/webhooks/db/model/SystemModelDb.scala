package com.LonelyDutchhound
package webhooks.db.model

import java.sql.Timestamp
import java.util.UUID

case class SystemModelDb(id: UUID,
                         dt_create: Timestamp,
                         dt_update: Option[Timestamp] = None,
                         dt_delete: Option[Timestamp] = None,
                         is_delete: Boolean = false)

object SystemModelDb {
  def dataColumn: List[Column] = List("id", "dt_create", "dt_update", "dt_delete", "is_delete")
}
