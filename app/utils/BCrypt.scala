package utils

import org.mindrot.jbcrypt.{BCrypt => jbcrypt}

/**
 * Created by kindone on 15. 4. 11..
 */
object BCrypt {
  class BCryptString(str:String)
  {
    def bcrypt() = jbcrypt.hashpw(str, jbcrypt.gensalt())

    def checkBcrypt(hashed:String) = jbcrypt.checkpw(str, hashed)
  }

  implicit def StringToBcryptString(str:String) = new BCryptString(str)

}
