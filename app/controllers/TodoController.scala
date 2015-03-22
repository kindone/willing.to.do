package controllers

import play.api.mvc._
import twentysix.playr._
import twentysix.playr.simple._
import play.api.libs.json.Json
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger


/**
 * TODO API
 *
 * POST: create new TODO.
 *
 *
 * PUT: update TODO. id must exist
 *
 *
 */

object TodoController extends RestCrudController[models.Todo.Composite] {

  def name = "todo"

  // in order to prevent Activate entity classes loading too early, val should be set lazy
  lazy val todoFormat = models.Todo.jsonFormat

  def fromId(sid: String): Option[models.Todo.Composite] = models.Todo.find(sid).map(_.frozen).map { t =>
    val deadline = t.deadlineId.flatMap(models.Reminder.find(_).map(_.frozen))
    val reminder = t.reminderId.flatMap(models.Reminder.find(_).map(_.frozen))
    val review = t.reviewId.flatMap(models.Reminder.find(_).map(_.frozen))
    val note = t.noteId.flatMap(models.Note.find(_).map(_.frozen))

    models.Todo.Composite(t.id, t.name, t.priority, t.willingness, t.tags, t.labels,
      t.context, t.category, deadline, reminder, review, note, t.parentId, t.ownerId)
  }

  def list() = Action {
    val todos = models.Todo.findAll.map(_.frozen).map { todo =>
      (todo.id, todo)
    }.toMap

    Ok(Json.toJson(todos.keys))
  }

  def read(todo: models.Todo.Composite) = Action { 
    implicit val format = todoFormat
    Ok(Json.toJson(todo)) 
  }

  def create = Action(parse.json) { request =>
    implicit val format = todoFormat
    val newTodo = request.body.as[models.Todo.Composite]
    Logger.info(newTodo.toString)
    val createdTodo = models.Todo.create(newTodo).frozen
    Created(Json.toJson(createdTodo.composite))
  }

  def write(todo: models.Todo.Composite) = Action(parse.json) { request =>
    implicit val format = todoFormat
    val newTodo = request.body.as[models.Todo.Composite].copy(id = todo.id)
    val updatedTodo = models.Todo.update(newTodo).frozen
    Ok(Json.toJson(updatedTodo.composite))
  }
  
  def delete(todo: models.Todo.Composite) = Action { request =>
    models.Todo.delete(todo.id.get)
    Ok(Json.toJson(""))
  }
  
}

object TodoRouter extends RestResourceRouter(TodoController) with ApiInfo