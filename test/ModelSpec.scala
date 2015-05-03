
import org.scalatest.junit.JUnitRunner
import org.scalatestplus.play._
import org.scalatest._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test.ActivateTest
import models.ActiveRecord._
/**
 * Created by kindone on 15. 3. 29..
 */
@RunWith(classOf[JUnitRunner])
class ModelSpec extends PlaySpec with OneAppPerSuite with ActivateTest {

  import models._

  "signup" in activateTest {

    val Some(testuser) = User.signup("testuser", "0000", "0000")

    testuser.username mustEqual "testuser"

  }

  "signup with duplicate name" should {
    "emit error" in activateTest {
      val Some(testuser) = User.signup("testuser", "0000", "0000")

      testuser.username mustEqual "testuser"

      a[net.fwbrasil.activate.entity.InvariantViolationException] must be thrownBy {
        User.signup("testuser", "0000", "0000")
      }
    }
  }

  "signup with mismatching passwords" should {
    "return None" in activateTest {

      User.signup("testuser", "0000", "0001") mustEqual None

    }
  }

  "login with passwords" should {
    "match user's password" in activateTest {

      val Some(newUser) = User.signup("testuser", "0000", "0000")
      val Some(foundUser) = User.authenticate("testuser", "0000")
      newUser.id.get mustEqual foundUser.id.get

      User.authenticate("testuser", "0001") mustEqual None
    }
  }

}
