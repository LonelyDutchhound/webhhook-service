package com.LonelyDutchhound
package webhooks.httpClient

import webhooks.httpClient.HttpClient.{HttpClientError, RemoteError, SystemError, TransportError}

import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.ssl.{SslContextBuilder, SslProvider}
import sttp.client.{Request, Response, SttpBackend}
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.model.Uri
import zio.logging.{Logging, log}
import zio.{Has, IO, Task, TaskLayer, ZIO, ZLayer}


class HttpClient(sttpBackend: SttpBackend[Task, Nothing, WebSocketHandler]) {

  def httpReq[T](request: Request[T, Nothing]): ZIO[Logging, HttpClientError, Response[T]] = {
    sttpBackend.send(request).flatMap(resp => {
      if (resp.is200) IO.succeed(resp)
      else if (resp.isServerError) IO.fail(RemoteError(request, resp))
      else IO.fail(TransportError(request, resp))
    })
      .tapError { ext => log.throwable(request.uri.toString(), ext) }
      .mapError {
        case ext: HttpClientError => ext
        case throwable => SystemError(request, throwable)
      }
  }
}


object HttpClient {

  type HasHttpClient = Has[HttpClient]

  sealed trait HttpClientError extends Throwable {
    val msg: String
  }

  case class RemoteError[T](request: Request[T, Nothing], response: Response[T], msg: String = "Backend internal error") extends HttpClientError {
    override def toString: String = s" RemoteError (${request.uri}) => code ${response.code} ${response.body}"
  }

  case class TransportError[T](request: Request[T, Nothing], response: Response[T], msg: String = "Fail to send the request to the backend") extends HttpClientError {
    override def toString: String = s" TransportError (${request.uri}) => code ${response.code} ${response.body}"
  }

  case class SystemError[T](request: Request[T, Nothing], exp: Throwable, msg: String = "Internal error") extends HttpClientError {
    override def toString: String = s" SystemError (${request.uri}) => ${exp.getMessage}"
  }


  val live: TaskLayer[HasHttpClient] =
    ZLayer.fromManaged(AsyncHttpClientZioBackend
      .managedUsingConfigBuilder(
        _.setSslContext(
          SslContextBuilder
            .forClient()
            .sslProvider(SslProvider.JDK)
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()
        )
      ).map(backend => new HttpClient(backend)))

  def getUriFromString(url: String*): ZIO[Any, Throwable, Uri] = IO.fromEither(Uri.parse(url.fold("")(_ + _)))
    .orElseFail(new Throwable("Error generate uri backend"))

  def httpReq[T](request: Request[T, Nothing]): ZIO[Logging with HasHttpClient, HttpClientError, Response[T]] =
    for {
      req <- ZIO.access[HasHttpClient](_.get)
      ret <- req.httpReq(request)
    } yield ret

}

