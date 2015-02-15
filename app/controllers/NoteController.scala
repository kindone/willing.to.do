package controllers

import play.api.mvc._
import twentysix.playr._
import twentysix.playr.simple._
import play.api.libs.json.Json
import play.api.libs.json.Reads._ 
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models.Note
import models.Note.Frozen

object NoteController extends RestReadController[Note.Frozen]
  with ResourceCreate with ResourceWrite {

  def name = "note"
  
  implicit val noteFormat = Json.format[Note.Frozen]

  def fromId(sid: String): Option[Note.Frozen] = Note.find(sid).map(_.frozen)

  def list() = Action {
    val notes = Note.findAll.map(_.frozen).map { note =>
      (note.id, note)
    }.toMap

    Ok(Json.toJson(notes.keys))
  }

  def read(note: Note.Frozen) = Action { Ok(Json.toJson(note)) }

  def create = Action(parse.json) { request =>
    val newNote = request.body.as[Note.Frozen]
    val createdNote = Note.create(newNote).frozen
    Created(Json.toJson(createdNote))
  }

  def write(note: Note.Frozen) = Action(parse.json) { request =>
    val newNote = request.body.as[Note.Frozen].copy(id = note.id)
    val createdNote = Note.update(newNote).frozen
    Ok(Json.toJson(createdNote))
  }
}

object NoteRouter extends RestResourceRouter(NoteController) with ApiInfo