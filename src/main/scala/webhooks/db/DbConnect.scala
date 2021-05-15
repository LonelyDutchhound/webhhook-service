package com.LonelyDutchhound
package webhooks.db

import cats.data.NonEmptyList
import cats.effect._
import doobie.Fragments._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.postgres.pgisimplicits._
import doobie.implicits.javasql._
import doobie.util.pos.Pos
import org.flywaydb.core.Flyway
import webhooks.config.AppConfig
import webhooks.config.GlobalCfg.HasConfig
import zio.blocking._
import zio.interop.catz._
import zio.logging.{Logging, log}
import zio.{blocking => _, _}
import scala.jdk.CollectionConverters._
import cats.implicits._

import java.util.UUID

object DbConnect {

  type HasDbConnect = Has[Service]

  case class DbConnectConfig(url: String, user: String, password: String, driver: String)

  case class Service(connect: HikariTransactor[Task]) {

    private def logMsg(command: String, startTime: Long, srcLine: Option[Pos], count: Int = 1): URIO[Logging, String] = for {
      t1 <- ZIO.effectTotal {
        System.currentTimeMillis() - startTime
      }
      sql =
        s"""Start sql :
           | Line: ${srcLine.map(_.toString).getOrElse("")}
           | ==================
           | $command
           | ==================
           | Count: $count
           | Time: $t1 ms""".stripMargin
      _ <- log.debug(sql)
    } yield sql


    def selectOne[O: Read](query: Query0[O]): ZIO[Logging, Throwable, O] = for {
      t0 <- ZIO.effectTotal(System.currentTimeMillis())
      ret <- query.unique
        .transact(connect)
        .tapError(ext => log.throwable(query.sql, ext))
      _ <- logMsg(query.sql, t0, query.pos)
    } yield ret

    def selectOne[O: Read](sql: String): RIO[Logging, O] = selectOne(Query0[O](sql, None))

    def selectOne[O: Read](sql: Fragment): RIO[Logging, O] = selectOne(sql.query[O])


    def select[O: Read](query: Query0[O]): ZIO[Logging, Throwable, List[O]] = for {
      t0 <- ZIO.effectTotal(System.currentTimeMillis())
      ret <- query.stream
        .compile
        .toList
        .transact(connect)
        .tapError(ext => log.throwable(query.sql, ext))
      _ <- logMsg(query.sql, t0, query.pos, ret.size)
    } yield ret

    def select[O: Read](sql: String): RIO[Logging, List[O]] = select(Query0[O](sql, None))

    def select[O: Read](sql: Fragment): RIO[Logging, List[O]] = select(sql.query[O])


    def update(query: Update0): ZIO[Logging, Throwable, Int] = for {
      t0 <- ZIO.effectTotal(System.currentTimeMillis())
      ret <- query.run.transact(connect)
        .tapError(expt => log.throwable(query.sql, expt))
      _ <- logMsg(query.sql, t0, query.pos, ret)
    } yield ret

    def update(sql: String): RIO[Logging, Int] = update(Update0(sql, None))

    def update(sql: Fragment): RIO[Logging, Int] = update(sql.update)


    def insert[O: Read](query: Update0, idColumn: String): ZIO[Logging, Throwable, O] = for {
      t0 <- ZIO.effectTotal(System.currentTimeMillis())
      ret <- query.withUniqueGeneratedKeys[O](idColumn).transact(connect)
        .tapError(expt => log.throwable(query.sql, expt))
      _ <- logMsg(query.sql, t0, query.pos)
    } yield ret

    def insert[TT: Write, O: Read](update: Update[TT], idColumn: String, data: List[TT]): ZIO[Logging, Throwable, List[O]] = for {
      t0 <- ZIO.effectTotal(System.currentTimeMillis())
      ret <- ZIO.effect(update.updateManyWithGeneratedKeys(idColumn)(data)
        .transact(connect)
      )
      retret <- ret.compile.toList
        .tapError(expt => log.throwable(update.sql, expt))
      _ <- logMsg(update.sql, t0, update.pos)
    } yield retret

    def insert(sql: String): RIO[Logging, UUID] = insert[UUID](Update0(sql, None), "id")

    def insert(sql: Fragment): RIO[Logging, UUID] = insert[UUID](sql.update, "id")


    def executeQuery[O](query: ConnectionIO[O]): Task[O] =
      query.transact(connect)


    val getVersion: RIO[Logging, String] = for {
      ret <- selectOne[String](sql"SELECT version();")
    } yield ret

  }


  val live: ZLayer[HasConfig with Blocking, Throwable, HasDbConnect] = ZLayer.fromManaged(
    for {
      liveEC <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blockEC <- blocking(ZIO.descriptor.map(_.executor.asEC)).toManaged_
      dbConfig <- AppConfig.get.map(_.dbConfig).toManaged_
      connect <- HikariTransactor.newHikariTransactor[Task](
        dbConfig.driver,
        dbConfig.url,
        dbConfig.user,
        dbConfig.password,
        liveEC,
        Blocker.liftExecutionContext(blockEC)
      ).toManagedZIO
      _ <- connect.configure { source =>
        ZIO.effect(Flyway.configure()
          .dataSource(source).load().migrate())
      }.toManaged_
    } yield Service(connect)

  )

}
