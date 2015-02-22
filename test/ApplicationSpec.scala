import org.scalatest.junit.JUnitRunner
import org.scalatestplus.play._
import org.scalatest._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpec with OneAppPerSuite{

  "Application" should {

    "send 404 on a bad request" in {
//      assertResult(None) {
        new App {
          route(FakeRequest(GET, "/boum"))
        }
//      }
    }

    "render the index page" in {
      new App {
        val home = route(FakeRequest(GET, "/")).get

        assert(status(home) === OK)
        contentType(home) must be (Some("text/html"))
        contentAsString(home) must contain("Your new application is ready.")
      }
    }
  }
}
