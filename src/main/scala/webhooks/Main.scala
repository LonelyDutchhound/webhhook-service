package com.LonelyDutchhound
package webhooks

import webhooks.config.GlobalCfg
import webhooks.db.DbConnect
import webhooks.db.service.WebhookDbService
import webhooks.kafka.KafkaProvider
import webhooks.logger.Logger
import webhooks.server.Server
import webhooks.service.ApiService

import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.duration.durationInt
import zio.logging.log

object Main extends App {

  val spaced: Schedule[Clock, Any, Long] = Schedule.spaced(1.hour)

  val program: ZIO[AppEnv, Throwable, Unit] =
    for {
      _ <- log.debug(Server.getMetric).repeat(spaced).forkDaemon
      srv <- Server.run
    } yield srv

  val layers: ZLayer[SystemEnv, Throwable, AppEnv] = {
    val sys = Blocking.live ++ Clock.live ++ Console.live
    val db = GlobalCfg.live ++ Blocking.live >>> DbConnect.live
    val kafka = GlobalCfg.live >>> KafkaProvider.live
    val service = sys ++ db ++ Logger.live ++ GlobalCfg.live
    service ++ kafka ++ ApiService.live ++ (db >>> WebhookDbService.live)
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {
    program
      .provideLayer(layers)
      .foldM(
        err => putStrLn(s"Execution failed with: $err").exitCode,
        _ => ZIO.succeed(ExitCode.success))
  }
}
