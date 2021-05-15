package com.LonelyDutchhound
package webhooks.service

import webhooks.service.ApiService.ErrorResponse

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.Tag
import sttp.tapir.ztapir._
import zio._

case class ServiceInfo(tagName: String, version: Double, describe: String)


trait RestServiceCore {
  def info: ServiceInfo

  def endpoints: Iterable[Endpoint[_, _, _, _]]

  def routes: ApiServiceEffect[HttpRoutes[Task]]

  def tag: Tag = Tag(info.tagName, Some(info.describe))

  protected val rootPath: Endpoint[Unit, ErrorResponse, Unit, Nothing] = sttp.tapir.endpoint
    .in("api" / info.version.toString)
    .tag(info.tagName)
    .errorOut {
      oneOf[ErrorResponse](
        statusMapping(StatusCode.BadRequest, jsonBody[ApiService.BadRequest].description("bad request")),
        statusMapping(StatusCode.Unauthorized, jsonBody[ApiService.AuthError].description("unauthorized")),
        statusMapping(StatusCode.NotFound, jsonBody[ApiService.NotFound].description("not found")),
        statusMapping(StatusCode.InternalServerError, jsonBody[ApiService.InternalError].description("Internal error")),
        statusDefaultMapping(jsonBody[ErrorResponse].description("unknown"))
      )
    }


}
