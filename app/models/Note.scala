package models

import ActiveRecord._
import play.api.libs.json.Json
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by kindone on 15. 1. 24..
 */
class Note(var title: Option[String], var content: String) extends Entity {
  def frozen = transactional {
    Note.Frozen(Some(id), title, content)
  }
}

object Note extends ActiveRecord[Note] {

  val jsonFormat = Json.format[Frozen]

  case class Frozen(id: Option[String], title: Option[String], content: String)

  def create(frozen: Frozen): Note = transactional {
    new Note(frozen.title, frozen.content)
  }

  def update(frozen: Frozen): Unit = transactional {
    val note = find(frozen.id.get).get
    note.title = frozen.title
    note.content = frozen.content
  }
}
