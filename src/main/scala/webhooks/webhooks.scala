package com.LonelyDutchhound

import webhooks.config.GlobalCfg.HasConfig
import webhooks.db.DbConnect.HasDbConnect
import webhooks.db.service.WebhookDbService.HasWebhookDbService
import webhooks.kafka.KafkaProvider.HasKafkaProvider
import webhooks.service.HasApiService

import zio.ZIO
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging

package object webhooks {

  type SystemEnv = Blocking with Clock with Console

  type ServiceEnv = SystemEnv with HasConfig with Logging with HasDbConnect with HasKafkaProvider

  type AppEnv = ServiceEnv
    with HasApiService with HasWebhookDbService

  type AppTaskIO[E, A] = ZIO[AppEnv, E, A]

  type AppTask[A] = AppTaskIO[Throwable, A]

}
