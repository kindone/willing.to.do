package models

import ActiveRecord._

/**
 * Created by kindone on 15. 3. 22..
 */


class User(var username: String, var password:String) extends Entity {
  def frozen() = transactional {
    User.Frozen(Some(id), username, password)
  }
}

object User extends ActiveRecord[User] {
  case class Frozen(id: Option[String], username: String, password:String)


  override def delete(id: String) {
    transactional {
      // delete deadline, reminder, review, context, category. note, parent
      val user = byId[User](id).get

      super.delete(id)
    }
  }
}


