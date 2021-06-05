package com.LonelyDutchhound
package webhooks.httpClient

import com.LonelyDutchhound.webhooks.httpClient.HttpClient.{HttpClientError, RemoteError, SystemError, TransportError}
import org.http4s.{Request, Response}
import zio.logging.{Logging, log}
import zio.{Has, IO, Task, TaskLayer, ZIO, ZLayer}


class HttpClient() {

  def httpReq[T](): ZIO[Logging, HttpClientError, Response[T]] = {
    httpBackend.send(request).flatMap(resp => {
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


  def httpReq[T](): ZIO[Logging with HasHttpClient, HttpClientError, Response[T]] =
    for {
      req <- ZIO.access[HasHttpClient](_.get)
      ret <- req.httpReq()
    } yield ret

}

