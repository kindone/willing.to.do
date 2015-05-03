package controllers

import play.api.libs.json.JsValue
import play.api.mvc._

/**
 * Created by kindone on 15. 5. 2..
 */
/* wrapper class for better usability */
trait Controller extends play.api.mvc.Controller {

  def bodyText(implicit request: Request[AnyContent]): String = request.body.asText.get

  def jsonParams(implicit request: Request[AnyContent]): JsValue = request.body.asJson.get

  def jsonParam(key: String)(implicit request: Request[AnyContent]): String = (jsonParams \ key).as[String]

  // params by query (usually used in GET method)
  def queryParams(implicit header: RequestHeader): Map[String, Seq[String]] = header.queryString

  def queryParam(key: String)(implicit header: RequestHeader): String = queryParams(header).get(key).get.head

  // form encoding (usually used in POST method)
  def formParams(implicit request: Request[AnyContent]): Map[String, Seq[String]] = request.body.asFormUrlEncoded.get

  def formParam(key: String)(implicit request: Request[AnyContent]): String = formParams.get(key).get.head

}