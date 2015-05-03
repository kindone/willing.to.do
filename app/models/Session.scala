package models

import models.ActiveRecord._

/**
 * Created by kindone on 15. 4. 18..
 */
class Session(val userId: String,
    val key: String,
    val value: String,
    var timeout: Int) extends Entity {
  def frozen = transactional {
    Session.Frozen(id, userId, key, value, timeout)
  }

  def invariantSessionKeyMustBeUnique =
    invariant {
      query {
        session: Session =>
          where((session.key :== key) :&&
            (session.id :!= this.id)) select (1)
      }.isEmpty
    }
}

object Session extends ActiveRecord[Session] {

  case class Frozen(id: String,
    userId: String,
    key: String,
    value: String,
    timeout: Int)

  def set(userId: String)(key: String, value: String, timeout: Int): Session.Frozen = transactional {
    new Session(userId, key, value, timeout).frozen
  }

  def get(key: String): Option[Session.Frozen] = transactional {
    val sessions = findByKey(key)
    sessions.headOption.map(_.frozen)
  }

  def unset(key: String): Option[Session.Frozen] = transactional {
    val session = select[Session] where (_.key :== key)
    val frozen = session.map(_.frozen)
    session.map(_.delete)
    frozen.headOption
  }

  def setTimeout(key: String, timeout: Int): Unit = transactional {
    findByKey(key).foreach {
      _.timeout = timeout
    }
  }

  def deleteAllForUser(userId: String): Unit = transactional {
    val sessions = select[Session] where (_.userId :== userId)
    sessions.foreach(_.delete)
  }

  private def findByKey(key: String): Option[Session] = transactional {
    (select[Session] where (_.key :== key)).headOption
  }
}
