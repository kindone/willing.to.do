package models

import ActiveRecord._
import play.api.libs.json.Json
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TodoTree(children: List[TodoNode])

case class TodoNode(todo: Option[Todo.Frozen], children: List[TodoNode])

// A todo can have multiple sub-todos
class Todo(var name: String,
    var priority: Int,
    var willingness: Int,
    var tags: Option[String],
    var labels: Option[String],
    var ctx: Option[String],
    var category: Option[String],
    var deadline: Option[Reminder],
    var reminder: Option[Reminder],
    var review: Option[Reminder],
    var note: Option[Note],
    var parent: Option[Todo],
    var owner: User,
    val status: Int) extends Entity {

  def frozen() = transactional {
    Todo.Frozen(Some(id),
      name,
      priority,
      willingness,
      tags,
      labels,
      ctx,
      category,
      deadline.map(_.id),
      reminder.map(_.id),
      review.map(_.id),
      note.map(_.id),
      parent.map(_.id),
      owner.id,
      status)
  }
}

object Todo extends ActiveRecord[Todo] {

  // status
  val ACTIVE = 0
  val DONE = 1
  val CANCELED = 2

  case class Frozen(id: Option[String],
      name: String,
      priority: Int,
      willingness: Int,
      tags: Option[String],
      labels: Option[String],
      context: Option[String],
      category: Option[String],
      deadlineId: Option[String],
      reminderId: Option[String],
      reviewId: Option[String],
      noteId: Option[String],
      parentId: Option[String],
      ownerId: String, status: Int) {

    def composite = transactional {

      val (deadline, reminder, review) =
        Seq(deadlineId, reminderId, reviewId).map { r =>
          r.flatMap(Reminder.find(_)).map(_.frozen)
        } match {
          case List(a, b, c) => (a, b, c)
        }

      val note = noteId.flatMap(Note.find(_)).map(_.frozen)

      Composite(id,
        name,
        priority,
        willingness,
        tags,
        labels,
        context,
        category,
        deadline,
        reminder,
        review,
        note,
        parentId,
        ownerId,
        status)
    }
  }

  case class Composite(id: Option[String],
    name: String,
    priority: Int,
    willingness: Int,
    tags: Option[String],
    labels: Option[String],
    context: Option[String],
    category: Option[String],
    deadline: Option[Reminder.Frozen],
    reminder: Option[Reminder.Frozen],
    review: Option[Reminder.Frozen],
    note: Option[Note.Frozen],
    parentId: Option[String],
    ownerId: String,
    status: Int)

  implicit val noteFormat = Note.jsonFormat
  implicit val reminderFormat = Reminder.jsonFormat
  val jsonFormat = Json.format[Composite]

  def create(todo: Composite): Todo = transactional {

    todo match {
      case Composite(id,
        name,
        priority,
        willingness,
        tags,
        labels,
        context,
        category,
        deadline,
        reminder,
        review,
        note,
        parentId,
        ownerId,
        status) =>

        val mDeadline = deadline.map(Reminder.create(_))
        val mReminder = reminder.map(Reminder.create(_))
        val mReview = review.map(Reminder.create(_))

        // create note if necessary
        val mNote = note.map { n =>
          new Note(n.title, n.content)
        }

        // find parent if exists
        val parent = parentId.flatMap(Todo.find(_))

        val owner = User.find(ownerId).get

        new Todo(name, priority, willingness, tags, labels, context, category,
          mDeadline, mReminder, mReview, mNote, parent, owner, status)
    }
  }

  def update(todo: Todo)(t: Composite): Todo = transactional {
    todo.name = t.name
    todo.priority = t.priority
    todo.willingness = t.willingness
    todo.tags = t.tags
    todo.labels = t.labels
    todo.ctx = t.context
    todo.category = t.category
    // todo.ownerId : changing owner is prohibited

    // create/update/delete according to the given

    Seq((todo.deadline, t.deadline),
      (todo.reminder, t.reminder),
      (todo.review, t.review)).foreach {
        case (oldr, newr) =>
          oldr match {
            case Some(original) => newr match {
              case Some(r) => Reminder.update(r.copy(id = Some(original.id)))
              case None    => Reminder.delete(original.id)
            }
            case None => newr.map(Reminder.create(_))
          }
      }

    todo.note match {
      case Some(original) => t.note match {
        case Some(n) => Note.update(n.copy(id = Some(original.id)))
        case None    => Note.delete(original.id)
      }
      case None => t.note.map(Note.create(_))
    }

    val parent = t.parentId.map(Todo.find(_).get)
    todo.parent = parent

    todo
  }

  override def delete(id: String) {
    transactional {
      // delete deadline, reminder, review, context, category. note, parent
      val todo = byId[Todo](id).get
      todo.deadline.map(e => Reminder.delete(e.id))
      todo.reminder.map(e => Reminder.delete(e.id))
      todo.review.map(e => Reminder.delete(e.id))
      todo.note.map(e => Note.delete(e.id))
      super.delete(id)
    }
  }
}

