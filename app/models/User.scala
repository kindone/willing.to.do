package models

import ActiveRecord._

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

class User(var username: String, var password:String, val role:String) extends Entity {
  def frozen() = transactional {
    val role_ = role match {
      case "administrator" => Administrator
      case "normal_user" => NormalUser
    }
    User.Frozen(Some(id), username, password, role_)
  }
}

object User extends ActiveRecord[User] {
  case class Frozen(id: Option[String], username: String, password:String, role:Role)

  def todos(id: String) = Todo.findAll()
  def createTodo(id: String, todo:Todo.Composite) = Todo.create(todo)
  def updateTodo(id: String, todo:Todo.Composite) = Todo.update(todo)
  def deleteTodo(id: String, todoId:String) = Todo.delete(todoId)

  override def delete(id: String) {
    transactional {
      val user = byId[User](id).get

      super.delete(id)
    }
  }

  def authenticate(username:String, password:String):Option[User.Frozen] = transactional {
    query {
      (user:User) => where((user.username :== username) :&& (user.password :== password)) select(user)
    }.headOption.map(_.frozen)
  }

  def signup(username:String, password:String, passwordconfirm:String):Option[User.Frozen] = transactional {
    if(password == passwordconfirm)
      Some(new User(username, password, NormalUser.toString()).frozen)
    else
      None
  }
}


