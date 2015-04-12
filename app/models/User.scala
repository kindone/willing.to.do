package models

import ActiveRecord._
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
           var hashedpassword:String,
           val role:String) extends Entity {

  def frozen() = transactional {
    val role_ = role match {
      case "administrator" => Administrator
      case "normal_user" => NormalUser
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
                    hashedpassword:String, role:Role)

  def todos(id: String) = Todo.findAll()
  def createTodo(id: String, todo:Todo.Composite) = Todo.create(todo)
  def updateTodo(id: String, todo:Todo.Composite) = Todo.update(todo)
  def deleteTodo(id: String, todoId:String) = Todo.delete(todoId)

  import utils.BCrypt._ // implicit conversion

  override def delete(id: String) {
    transactional {
      val user = byId[User](id).get

      super.delete(id)
    }
  }

  def authenticate(username:String, password:String):Option[User.Frozen] = transactional {
    select[User].where(_.username :== username)
      .filter(user => password.checkBcrypt(user.hashedpassword))
      .headOption.map(_.frozen)
  }

  def signup(username:String, password:(String, String)):Option[User.Frozen] = transactional {
    if(password._1 == password._2) {
      try {
        Some(new User(username, password._1.bcrypt, NormalUser.toString()).frozen)
      }
      catch {
        case e:InvariantViolationException =>
          None
      }
    }
    else
      None
  }

  def update(id:String)
            (oldpassword:String, password:(String,String)):Option[User.Frozen] = transactional {
    if(password._1 == password._2) {
      User.find(id).filter(user => oldpassword.checkBcrypt(user.hashedpassword)).headOption.map { user =>
        user.hashedpassword = password._1.bcrypt
        user.frozen()
      }
    }
    else
      None
  }
}


