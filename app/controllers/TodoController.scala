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

object TodoController extends RestCrudController[models.Todo.Composite]
    with Controller with AuthElement with AuthConfigImpl {

  def name = "todo"

  // in order to prevent Activate entity classes loading too early,
  // val should be set lazy
  lazy implicit val todoFormat = models.Todo.jsonFormat

  def todoFromMap(userId: String)(map: Map[String, Seq[String]]): models.Todo.Composite = {

    val name = map.get("name").get.head
    val priority = map.get("priority").getOrElse(Seq("0")).head.toInt
    val willingness = map.get("willingness").getOrElse(Seq("0")).head.toInt

    models.Todo.Composite(None, name, priority, willingness,
      None, None, None, None, None, None, None, None, None,
      userId, models.Todo.ACTIVE)
  }

  def fromId(sid: String): Option[models.Todo.Composite] =
    models.Todo.find(sid).map(_.frozen).map { t =>
      val deadline = t.deadlineId.flatMap(models.Reminder.find(_).map(_.frozen))
      val reminder = t.reminderId.flatMap(models.Reminder.find(_).map(_.frozen))
      val review = t.reviewId.flatMap(models.Reminder.find(_).map(_.frozen))
      val note = t.noteId.flatMap(models.Note.find(_).map(_.frozen))

      models.Todo.Composite(t.id, t.name, t.priority, t.willingness,
        t.tags, t.labels,
        t.context, t.category, deadline, reminder, review, note,
        t.parentId, t.ownerId, t.status)
    }

  def list() = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val todos =
      models.User.activeTodos(loggedIn.id.get).map(_.frozen).map { todo =>
        (todo.id, todo)
      }.toMap

    Ok(Json.toJson(todos.keys))
  }

  def read(todo: models.Todo.Composite) =
    StackAction(AuthorityKey -> NormalUser) { implicit request =>
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

  def write(todo: models.Todo.Composite) =
    StackAction(parse.json, AuthorityKey -> NormalUser) { implicit request =>
      val newTodo = request.body.as[models.Todo.Composite].copy(id = todo.id)
      val updatedTodo = models.User.updateTodo(loggedIn.id.get, newTodo).frozen
      Ok(Json.toJson(updatedTodo.composite))
    }

  def delete(todo: models.Todo.Composite) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    models.User.deleteTodo(loggedIn.id.get, todo.id.get)
    Ok(Json.toJson(""))
  }

  //  def projects = StackAction(AuthorityKey -> NormalUser) { implicit request =>
  //    val ps = models.User.activeRootTodos(loggedIn.id.get).map(_.frozen)
  //    Ok(Json.toJson(ps))
  //  }
  //
  //  def subTodos(id: String) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
  //    val ps = models.User.activeSubTodos(loggedIn.id.get, id).map(_.frozen)
  //    Ok(Json.toJson(ps))
  //  }

}

object TodoRouter extends RestResourceRouter(TodoController) with ApiInfo