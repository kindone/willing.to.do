package models

import ActiveRecord._
import models.Todo._
import net.fwbrasil.activate.entity.InvariantViolationException
import org.mindrot.jbcrypt.BCrypt

/**
 * Created by kindone on 15. 3. 22..
 */

sealed trait Role

case object Administrator extends Role {
  override def toString() = "administrator"
}

case object NormalUser extends Role {
  override def toString() = "normal_user"
}

class User(var username: String,
    var hashedpassword: String,
    val role: String) extends Entity {

  def frozen(): User.Frozen = transactional {
    val role_ = role match {
      case "administrator" => Administrator
      case "normal_user"   => NormalUser
    }
    User.Frozen(Some(id), username, hashedpassword, role_)
  }

  def invariantUsernameMustBeUnique =
    invariant {
      query {
        user: User =>
          where((user.username :== username) :&&
            (user.id :!= this.id)) select (1)
      }.isEmpty
    }
}

object User extends ActiveRecord[User] {

  case class Frozen(id: Option[String],
    username: String,
    hashedpassword: String, role: Role)

  import utils.BCrypt._

  def allTodos(id: String): List[Todo] = transactional {
    select[Todo] where (todo => (todo.owner.id :== id))
  }

  def activeTodos(id: String): List[Todo] = transactional {
    select[Todo] where (todo => (todo.owner.id :== id) :&& (todo.status :== Todo.ACTIVE))
  }

  def activeRootTodos(id: String): List[Todo] = transactional {
    select[Todo] where (todo => (todo.owner.id :== id) :&& (todo.parent.isNull) :&& (todo.status :== Todo.ACTIVE))
  }

  def inbox(id: String): List[Todo] = activeRootTodos(id)

  def activeTodoTree(id: String): TodoTree = transactional {

    def subTree(todos: List[Todo], level: Int = 0): List[TodoNode] = transactional {
      todos.map(_.frozen).map { todo =>
        val sub = activeSubTodos(id, todo.id.get)
        TodoNode(Some(todo), subTree(sub, level + 1))
      }
    }

    val rootTodos = activeRootTodos(id)
    val nodes: List[TodoNode] = subTree(rootTodos)

    TodoTree(nodes.filterNot(_.children.isEmpty))
    //
    //    val uncategorized: TodoNode = TodoNode(None, nodes.filter(_.children.isEmpty))
    //    val projects: List[TodoNode] = nodes.filterNot(_.children.isEmpty)
    //
    //    TodoTree(uncategorized +: projects)
  }

  def activeSubTodos(id: String, todoId: String) = transactional {
    val todo = Todo.find(todoId).get
    select[Todo] where (todo => (todo.owner.id :== id) :&& (todo.parent :== todo))
  }

  def createTodo(id: String, todo: Todo.Composite): Option[Todo] = {
    if (todo.ownerId == id)
      Some(Todo.create(todo))
    else
      None
  }

  def updateTodo(id: String, composite: Todo.Composite): Todo = {
    val todo = select[Todo] where (t => (t.owner.id :== id) :&& (t.id :== composite.id))
    Todo.update(todo.head)(composite)
  }

  def deleteTodo(id: String, todoId: String): Unit = {
    val todo = select[Todo] where (t => (t.owner.id :== id) :&& (t.id :== todoId))
    todo.foreach(_.delete)
  }

  override def delete(id: String) {
    transactional {
      val user = byId[User](id).get
      // TODO: remove all todos owned by user
      super.delete(id)
    }
  }

  def authenticate(username: String, password: String): Option[User.Frozen] = transactional {
    select[User].where(_.username :== username)
      .filter(user => password.checkBcrypt(user.hashedpassword))
      .headOption.map(_.frozen)
  }

  def signup(username: String, password: (String, String)): Option[User.Frozen] = transactional {
    if (password._1 == password._2) {
      try {
        Some(new User(username, password._1.bcrypt, NormalUser.toString()).frozen)
      } catch {
        case e: InvariantViolationException =>
          None
      }
    } else
      None
  }

  def update(id: String)(oldpassword: String, password: (String, String)): Option[User.Frozen] = transactional {
    if (password._1 == password._2) {
      User.find(id).filter(user => oldpassword.checkBcrypt(user.hashedpassword)).headOption.map { user =>
        user.hashedpassword = password._1.bcrypt
        user.frozen()
      }
    } else
      None
  }
}

