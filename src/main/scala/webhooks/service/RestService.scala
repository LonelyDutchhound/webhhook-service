package com.LonelyDutchhound
package webhooks.service

import webhooks.AppEnv
import webhooks.server.Server
import webhooks.service.ApiService.{BadRequest, ErrorResponse, ErrorWithInfo, InternalError}

import org.http4s.HttpRoutes
import org.http4s.metrics.dropwizard.Dropwizard
import sttp.tapir.Endpoint
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, URIO, ZIO}
import sttp.tapir.server.http4s.ztapir._
import zio.interop.catz._
import zio.interop.catz.implicits._
import cats.implicits._
import org.http4s.server.middleware.Metrics

trait RestService[R <: AppEnv] extends RestServiceCore with RestDirective {

  type CoreEnv = R
  type RestTask[A] = ZIO[CoreEnv, Throwable, A]
  type RestTaskSpec[R1, A] = ZIO[CoreEnv with R1, Throwable, A]

  protected val services: List[ZServerEndpoint[CoreEnv, _, ErrorResponse, _]]

  override def endpoints: Iterable[Endpoint[_, _, _, _]] = services.map(_.endpoint)

  private def apiEndpointToRoute(ep: ZServerEndpoint[CoreEnv, _, ErrorResponse, _]): ZIO[CoreEnv, Throwable, HttpRoutes[Task]] = {
    val route: URIO[CoreEnv, HttpRoutes[Task]] = {
      ep.toRoutesR
    }
    route.flatMap { r =>
      ZIO.effect(Metrics[Task](Dropwizard(Server.registry, ep.endpoint.getSystemName))(r))
    }
  }


  protected def routes_ : ZIO[CoreEnv, Throwable, HttpRoutes[Task]] = {
    ZIO.foreach(services)(apiEndpointToRoute)
      .map((r: Seq[HttpRoutes[Task]]) =>
        r.foldLeft(HttpRoutes.empty[Task])((ac: HttpRoutes[Task], i: HttpRoutes[Task]) => ac <+> i))
  }


  implicit def handleError(err: Throwable): ErrorResponse = err match {
    case err: ErrorWithInfo =>
      BadRequest(err.msg)
    case err: ErrorResponse =>
      err
    case _ =>
      InternalError("unknown")
  }

}
