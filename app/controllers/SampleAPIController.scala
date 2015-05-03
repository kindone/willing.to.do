package controllers

import play.api.mvc._
import twentysix.playr._
import twentysix.playr.simple._
import play.api.libs.json.Json
import play.api.Logger

case class SampleModel(id: Int, name: String, address: Option[String])

object SampleAPIController extends RestReadController[SampleModel]
    with ResourceCreate with ResourceWrite {

  def name = "todo"

  val items = List(SampleModel(1, "Aaron", None), SampleModel(2, "Banks", None), SampleModel(3, "Curkes", None))

  implicit val todoFormat = Json.format[SampleModel]

  def fromId(sid: String): Option[SampleModel] = items.find { item => item.id.toString == sid }

  def list() = Action {
    val todos = items.map { item =>
      (item.id, item)
    }.toMap

    Ok(Json.toJson(todos.keys))
  }

  def read(todo: SampleModel) = Action { Ok(Json.toJson(todo)) }

  def create = Action(parse.json) { request =>
    val newItem = request.body.as[SampleModel]
    Created(Json.toJson(newItem))
  }

  def write(todo: SampleModel) = Action(parse.json) { request =>
    val newItem = request.body.as[SampleModel].copy()
    Ok(Json.toJson(newItem))
  }
}

object SampleAPIRouter extends RestResourceRouter(SampleAPIController) with ApiInfo