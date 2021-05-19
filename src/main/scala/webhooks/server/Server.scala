package com.LonelyDutchhound
package webhooks.server

import webhooks.config.AppConfig
import webhooks.service.HasApiService
import webhooks.{AppEnv, AppTask}

import cats.effect._
import com.codahale.metrics.{MetricRegistry, SharedMetricRegistries}
import org.http4s.implicits._
import org.http4s.metrics.dropwizard.Dropwizard
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Logger, Metrics}
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

case class ServerConfig(
                         port: Int,
                         address: String
                       )

object Server {
  implicit val clock: Clock[cats.effect.IO] = Clock.create[cats.effect.IO]
  val registry: MetricRegistry = SharedMetricRegistries.getOrCreate("default")

  val cORSConfig: CORSConfig = CORSConfig(
    anyOrigin = true,
    anyMethod = true,
    allowedOrigins = Set("*"),
    allowedMethods = Some(Set("GET", "POST", "PUT", "DELETE", "OPTION")),
    allowCredentials = true,
    maxAge = 1.day.toSeconds)

  def getMetric: String = {
    (
      registry.getCounters().asScala.map { case (key, value) => s"$key = ${value.getCount}" } ++
      registry.getMeters().asScala.map { case (key, value) => s"$key = ${value.getCount}" } ++
      registry.getTimers().asScala.map { case (key, value) => s"$key = ${value.getCount}" }
    ).mkString("\n")
  }

  val run: AppTask[Unit] = {
    for {
      config <- AppConfig.get
      route <- ZIO.access[HasApiService](_.get.services)
      routes <- route


      server <- ZIO.runtime[AppEnv].flatMap {
        implicit rts =>
          val r = CORS(Router("/" -> Metrics[Task](Dropwizard(registry, "server"))(routes)), cORSConfig).orNotFound
          val logger = Logger.httpApp(logHeaders = false, logBody = false)(r)
          BlazeServerBuilder[Task](rts.platform.executor.asEC)
            .withIdleTimeout(10.minute)
            .withResponseHeaderTimeout(5.minute)
            .bindHttp(config.server.port, config.server.address)
            .withHttpApp(logger)
            .serve
            .compile
            .drain
      }
    } yield server
  }
}