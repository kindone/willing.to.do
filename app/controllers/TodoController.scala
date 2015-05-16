package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.NormalUser
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

object TodoController extends Controller with AuthElement with AuthConfigImpl {

  // in order to prevent Activate entity classes loading too early,
  // val should be set lazy
  lazy implicit val todoFormat = models.Todo.jsonFormat

  private def todoFromMap(userId: String)(map: Map[String, Seq[String]]): models.Todo.Composite = {

    val name = map.get("name").get.head
    val priority = map.get("priority").getOrElse(Seq("0")).head.toInt
    val willingness = map.get("willingness").getOrElse(Seq("0")).head.toInt

    models.Todo.Composite(None, name, priority, willingness,
      None, None, None, None, None, None, None, None, None,
      userId, 0, models.Todo.ACTIVE)
  }
  /*
  def list() = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val todos =
      models.User.activeTodos(loggedIn.id.get).map(_.frozen).map { todo =>
        (todo.id, todo)
      }.toMap

    Ok(Json.toJson(todos.keys))
  }
*/

  def read(id: String) =
    StackAction(AuthorityKey -> NormalUser) { implicit request =>
      val todo = models.Todo.find(id).map(_.frozen).map { t =>
        val deadline = t.deadlineId.flatMap(models.Reminder.find(_).map(_.frozen))
        val reminder = t.reminderId.flatMap(models.Reminder.find(_).map(_.frozen))
        val review = t.reviewId.flatMap(models.Reminder.find(_).map(_.frozen))
        val note = t.noteId.flatMap(models.Note.find(_).map(_.frozen))

        models.Todo.Composite(t.id, t.name, t.priority, t.willingness,
          t.tags, t.labels,
          t.context, t.category, deadline, reminder, review, note,
          t.parentId, t.ownerId, t.position, t.status)
      }
      Ok(Json.toJson(todo))
    }

  def create =
    StackAction(parse.urlFormEncoded, AuthorityKey -> NormalUser) { implicit request =>
      val newTodo = todoFromMap(loggedIn.id.get)(request.body)
      //val newTodo = request.body.as[models.Todo.Composite]
      val createdTodo =
        models.User.createTodo(loggedIn.id.get, newTodo).get.frozen
      Created(Json.toJson(createdTodo.composite))
    }

  def update(id: String) =
    StackAction(parse.json, AuthorityKey -> NormalUser) { implicit request =>
      val newTodo = request.body.as[models.Todo.Composite].copy(id = Some(id))
      val updatedTodo = models.User.updateTodo(loggedIn.id.get, newTodo).frozen
      Ok(Json.toJson(updatedTodo.composite))
    }

  def delete(id: String) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    models.User.deleteTodo(loggedIn.id.get, id)
    Ok(Json.toJson(""))
  }

}
