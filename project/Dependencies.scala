import sbt._

import scala.languageFeature.postfixOps

object Dependencies {

  object V {

    val zio = "1.0.0"
    val zioCats = "2.1.4.0"
    val http4s = "0.21.4"
    val logback = "1.2.3"
    val scalaLogging = "3.9.2"
    val sl4j = "1.7.25"
    val http4scirce = "0.21.4"
    val tapir = "0.14.5"
    val circe = "0.13.0"
    val doobie = "0.8.8"
    val clientSttp = "2.1.1"
    val ziologging = "0.3.1"
    val flyway = "6.0.8"
    val pureConfig = "0.12.1"
    val ziokafka = "0.14.0"
    val ziostreams = "1.0.2"
  }

  object KAFKA {
    lazy val streams = "dev.zio" %% "zio-streams" % V.ziostreams
    lazy val kafka = "dev.zio" %% "zio-kafka"   % V.ziokafka
  }

  object ZIO {
    lazy val core = "dev.zio" %% "zio" % V.zio
    lazy val cats = "dev.zio" %% "zio-interop-cats" % V.zioCats
  }

  object HTTP {
    lazy val core = "org.http4s" %% "http4s-core" % V.http4s
    lazy val basel = "org.http4s" %% "http4s-blaze-server" % V.http4s
    lazy val dsl = "org.http4s" %% "http4s-dsl" % V.http4s
    lazy val http4scirce = "org.http4s" %% "http4s-circe" % V.http4scirce
    lazy val circe = "io.circe" %% "circe-generic" % V.circe
  }

  object TAPIR {
    lazy val tapir = "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir
    lazy val tapirhttp4s = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir
    lazy val tapircirce = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir
    lazy val tapirdocs = "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % V.tapir
    lazy val tapircirceyaml = "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % V.tapir
    lazy val tapirui ="com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % V.tapir
    lazy val tapirzio = "com.softwaremill.sttp.tapir" %% "tapir-zio" % "0.15.3"
    lazy val tapirhtt4szio ="com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % "0.15.3"
    lazy val tapirhttpclient = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.1.5"

  }

  object LOGS {
    lazy val core = "ch.qos.logback" % "logback-classic" % V.logback
    lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % V.scalaLogging
    lazy val sl4j = "org.slf4j" % "slf4j-api" % V.sl4j
    lazy val ziologging =  "dev.zio" %% "zio-logging" % V.ziologging
    lazy val ziologgingslf4j = "dev.zio" %% "zio-logging-slf4j" % V.ziologging
  }

  object CONFIG {
    lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig
  }

  object METRICS {
    lazy val dropwizard  = "org.http4s" %% "http4s-dropwizard-metrics" % V.http4s
    lazy val prometheus = "io.prometheus" % "client" % "0.0.10"
    lazy val simpleclient_pushgateway = "io.prometheus" % "simpleclient_pushgateway" % "0.9.0"
    lazy val metrics = "org.http4s" %% "http4s-prometheus-metrics" % V.http4s
  }

  object DB {
    lazy val core = "org.tpolecat" %% "doobie-core"      % V.doobie
    lazy val postgres ="org.tpolecat" %% "doobie-postgres"  % V.doobie
    lazy val hikari = "org.tpolecat" %% "doobie-hikari"    % V.doobie          // HikariCP transactor.
    lazy val scalatest ="org.tpolecat" %% "doobie-scalatest" % V.doobie % "test"  // ScalaTest support for typechecking statements.
    lazy val flyway = "org.flywaydb" % "flyway-core" % V.flyway
  }


  object TEST {
    val zioTest = "dev.zio" %% "zio-test"          % V.zio % "test"
    val zioTestSbt = "dev.zio" %% "zio-test-sbt"      %  V.zio % "test"
  }

  lazy val globalProjectDeps = Seq(
    KAFKA.kafka,
    KAFKA.streams,

    ZIO.core,
    ZIO.cats,

    HTTP.core,
    HTTP.basel,
    HTTP.dsl,
    HTTP.circe,
    HTTP.http4scirce,

    TAPIR.tapir,
    TAPIR.tapirhttp4s,
    TAPIR.tapircirce,
    TAPIR.tapirdocs,
    TAPIR.tapircirceyaml,
    TAPIR.tapirui,
    TAPIR.tapirzio,
    TAPIR.tapirhtt4szio,

    DB.core,
    DB.hikari,
    DB.postgres,
    DB.scalatest,
    DB.flyway,

    LOGS.core,
    LOGS.scalaLogging,
    LOGS.sl4j,
    LOGS.ziologging,
    LOGS.ziologgingslf4j,

    METRICS.dropwizard,
    METRICS.prometheus,
    METRICS.simpleclient_pushgateway,
    METRICS.metrics,

    CONFIG.pureConfig,

    TEST.zioTest,
    TEST.zioTestSbt
  )
}