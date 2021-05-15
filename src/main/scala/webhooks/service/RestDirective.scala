package com.LonelyDutchhound
package webhooks.service

import webhooks.AppEnv
import webhooks.logger.Logger
import webhooks.service.ApiService.ErrorResponse

import sttp.tapir.ztapir.{ZEndpoint, ZServerEndpoint, _}
import zio.ZIO
import zio.logging.log

import java.util.UUID

trait RestDirective {


  implicit class CustZEndpoint[I, O](e: ZEndpoint[I, ErrorResponse, O]) {
    val getSystemName: String = {
      e.info.tags.mkString(".") + "." + e.info.name.getOrElse("None")
    }

    def logic[R <: AppEnv](logic: I => ZIO[R, Throwable, O])
                          (implicit err: Throwable => ErrorResponse)
    : ZServerEndpoint[R, I, ErrorResponse, O] = {
      e.zServerLogic { i =>
        log.locally(_.annotate(Logger.method, Some(e.getSystemName))
          .annotate(Logger.traceId, UUID.randomUUID())) {
          log.info(s"Start request with $i") *>
            logic(i)
        }.tapError { ext => log.throwable("Server logic error: ", ext) }
          .mapError(err)
      }
    }
  }

}
