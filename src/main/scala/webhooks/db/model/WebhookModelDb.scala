package com.LonelyDutchhound
package webhooks.db.model

import doobie.implicits._
import doobie.postgres.implicits._
import doobie.implicits.javatime._
import doobie.implicits.javasql._
import doobie.util.fragments.set
import doobie.{Fragment, Fragments}

import java.util.UUID

case class WebhookModelDb (
                            name: String,
                            url: String
                          )

object WebhookModelDb extends ModelDb[WebhookModelDb]{

  override def dataColumn: List[Column] = List("name", "url")

  override def tableName: String = "webhook"

  def update(id: UUID, name: Option[String], url: Option[String]) = {
    val update = Fragment.const(s"UPDATE $tableName SET ")
    val setName = name.map(v => fr"name=$v")
    val setUrl = url.map(v => fr"url=$v")
    val setAll = (setName ++ setUrl).reduceLeftOption((f1, f2) => f1 ++ fr", " ++ f2)
    setAll.map(update ++ _ ++ Fragments.whereAnd(fr"id=$id")).map(_.update)
  }

  def selectById(id: UUID) = select ++ Fragments.whereAnd(fr"id=$id")
}
