import org.scalatestplus.play.{ HtmlUnitFactory, OneServerPerSuite, OneBrowserPerSuite, PlaySpec }
import org.junit.runner._
import org.scalatest.junit.JUnitRunner
import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory {
  //override lazy val app = FakeApplication()

  "Application" should {

    "work from within a browser" in new WithBrowser(webDriver = WebDriverFactory(HTMLUNIT), app = app) {

      browser.goTo("http://localhost:" + port)

      browser.pageSource must contain("Willing.to.do")
    }
  }

  "Standard Sample" should {
    "work from within a browser" in new WithBrowser(webDriver = WebDriverFactory(HTMLUNIT), app = FakeApplication(additionalConfiguration = inMemoryDatabase(name = "default", options = Map("DB_CLOSE_DELAY" -> "-1")))) {
      val baseURL = s"http://localhost:${port}"
      // login failed
      browser.goTo(baseURL)
      browser.$("#email").text("alice@example.com")
      browser.$("#password").text("secretxxx")
      browser.$("#loginbutton").click()
      browser.pageSource must contain("Invalid email or password")
      // login succeded
      browser.$("#email").text("alice@example.com")
      browser.$("#password").text("secret")
      browser.$("#loginbutton").click()
      browser.$("dl.error").size mustEqual 0
      browser.pageSource must not contain ("Sign in")
      browser.pageSource must contain("logout")
      browser.getCookie("PLAY2AUTH_SESS_ID").getExpiry must not be null
      // logout
      browser.$("a").click()
      browser.pageSource must contain("Sign in")
      browser.goTo(s"$baseURL/standard/messages/write")
      browser.pageSource must contain("Sign in")
    }
    "authorize" in new WithBrowser(webDriver = WebDriverFactory(HTMLUNIT), app = FakeApplication(additionalConfiguration = inMemoryDatabase(name = "default", options = Map("DB_CLOSE_DELAY" -> "-1")))) {
      val baseURL = s"http://localhost:${port}"
      // login succeded
      browser.goTo(baseURL)
      browser.$("#email").text("bob@example.com")
      browser.$("#password").text("secret")
      browser.$("#loginbutton").click()
      browser.$("dl.error").size mustEqual 0
      browser.pageSource must not contain ("Sign in")
      browser.pageSource must contain("logout")
      browser.goTo(s"${baseURL}/standard/messages/write")
      browser.pageSource must contain("no permission")
      browser.goTo(s"${baseURL}/standard/logout")
      browser.$("#email").text("alice@example.com")
      browser.$("#password").text("secret")
      browser.$("#loginbutton").click()
      browser.$("dl.error").size mustEqual 0
      browser.goTo(s"${baseURL}/standard/messages/write")
      browser.pageSource must not contain ("no permission")
    }
  }
}
