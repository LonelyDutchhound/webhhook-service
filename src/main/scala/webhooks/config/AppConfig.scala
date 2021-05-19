package com.LonelyDutchhound
package webhooks.config

import webhooks.config.GlobalCfg.HasConfig
import webhooks.db.DbConnect.DbConnectConfig
import webhooks.server.ServerConfig

import com.LonelyDutchhound.webhooks.kafka.KafkaConfig
import zio.{URIO, ZIO}

case class AppConfig(
                      server: ServerConfig,
                      dbConfig: DbConnectConfig,
                      kafkaConfig: KafkaConfig
                    )


object AppConfig {

  def get: URIO[HasConfig, AppConfig] = ZIO.environment[HasConfig].flatMap(_.get.getConfig)

}