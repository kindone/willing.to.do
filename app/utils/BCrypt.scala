package utils

import org.mindrot.jbcrypt.{ BCrypt => jbcrypt }

/**
 * Created by kindone on 15. 4. 11..
 */
object BCrypt {

  class BCryptString(str: String) {
    def bcrypt(): String = jbcrypt.hashpw(str, jbcrypt.gensalt())

    def checkBcrypt(hashed: String): Boolean = jbcrypt.checkpw(str, hashed)
  }

  implicit def stringToBcryptString(str: String): BCryptString = new BCryptString(str)

}
