package com.LonelyDutchhound
package webhooks.service

import webhooks.{AppEnv, AppTask}

import cats.effect.Clock
import sttp.tapir._
import org.http4s.HttpRoutes
import zio.{Task, ULayer, ZIO, ZLayer}
import cats._
import cats.implicits._
import zio.interop.catz._
import zio._

class ApiService {

  val services: AppTask[HttpRoutes[Task]] = ZIO.foreach(servicesApi)(_.routes)
    .flatMap {
      r =>
        val ret = r.iterator.reduce((ac: HttpRoutes[Task], i: HttpRoutes[Task]) => ac <+> i)
        ZIO.effect(ret)
    }

}

object ApiService {


  val live: ULayer[HasApiService] = ZIO.effectTotal(new ApiService).toLayer


  sealed abstract class ErrorResponse(msg: String = "Internal error", description: String = "") extends Throwable {
    override def toString: String = msg

    override def getMessage: String = toString
  }

  final case class AuthError(info: String) extends ErrorResponse("Access deny", info)

  final case class BadRequest(info: String) extends ErrorResponse("Bad request", info)

  final case class NotFound(info: String) extends ErrorResponse("NotFound", info)


  final case class InternalError(info: String) extends ErrorResponse(description = info)

  case class ErrorWithInfo(msg: String) extends Throwable
  {
    override def toString: String = msg

    override def getMessage: String = toString
  }

}
