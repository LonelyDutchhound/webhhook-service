package com.LonelyDutchhound
package webhooks.logger

import org.slf4j.{LoggerFactory, MDC, Logger => slf4logger}
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, LogContext, Logging}
import zio.{ULayer, ZIO}

import java.util.UUID
import scala.jdk.CollectionConverters.MapHasAsJava

object Logger {

  case class UnsafeLogger(logging: slf4logger, context: LogContext) {
    def info(string: String): Unit = {
      MDC.setContextMap(context.renderContext.asJava)
      logging.info(string)
    }

    def error(string: String): Unit = {
      MDC.setContextMap(context.renderContext.asJava)
      logging.error(string)
    }
  }

  val traceId: LogAnnotation[UUID] = LogAnnotation[UUID](
    name = "trace-id",
    initialValue = UUID.fromString("0-0-0-0-0"),
    combine = (_, newValue) => newValue,
    render = _.toString
  )
  val method: LogAnnotation[Option[String]] = LogAnnotation[Option[String]](
    name = "method",
    initialValue = None,
    combine = (_, newValue) => newValue,
    render = _.getOrElse("None")
  )
  val live: ULayer[Logging] = Slf4jLogger.makeWithAnnotationsAsMdc(List(traceId, method))

  def getUnsafe[R1](c: Class[_]): ZIO[Logging, Throwable, UnsafeLogger] = for {
    log <- ZIO.access[Logging](_.get)
    context <- log.logContext
    l <- ZIO.effect {
      val logger = LoggerFactory.getLogger(c.getName)
      UnsafeLogger(logger, context)
    }
  } yield l


}
