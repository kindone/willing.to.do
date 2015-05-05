package controllers

import controllers.TodoController._
import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.{ OptionalAuthElement, LoginLogout }
import play.api.data._
import play.api.data.Forms._
import models.User
import views.html
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Application extends Controller with LoginLogout with OptionalAuthElement with AuthConfigImpl {

  def index = StackAction { implicit request =>
    loggedIn match {
      case Some(user) =>
        val tree = models.User.activeTodoTree(loggedIn.get.id.get)
        Ok(views.html.index("Your new application is ready.", tree))
      case None =>
        Ok(views.html.intro(formForLogin, formForSignup))
    }
  }

  // better remove and move to ajax-style one (included in index)
  def signupForm = StackAction { implicit request =>
    loggedIn match {
      case Some(_) => Redirect(routes.Application.index())
      case None    => Ok(views.html.signup(formForSignup))
    }
  }

  /** Your application's login form.  Alter it to fit your application */
  val formForLogin = Form {
    mapping("username" -> email, "password" -> text)(User.authenticate)(_.map(u => (u.username, "")))
      .verifying("Invalid username or password", result => result.isDefined)
  }

  val formForSignup = Form {
    mapping("username" -> email,
      "password" -> tuple("main" -> text, "confirm" -> text).verifying("password and confirmation does not match", p => p._1 == p._2)
    )(User.signup)(_.map(u => (u.username, ("", ""))))
      .verifying("Invalid username", result => result.isDefined)
  }

  /**
   * Return the `gotoLoginSucceeded` method's result in the login action.
   *
   * Since the `gotoLoginSucceeded` returns `Future[Result]`,
   * you can add a procedure like the `gotoLogoutSucceeded`.
   */
  def authenticate = Action.async { implicit request =>
    formForLogin.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.intro(formWithErrors, formForSignup))),
      user => gotoLoginSucceeded(user.get.id.get)
    )
  }

  /**
   * Return the `gotoLogoutSucceeded` method's result in the logout action.
   *
   * Since the `gotoLogoutSucceeded` returns `Future[Result]`,
   * you can add a procedure like the following.
   *
   *   gotoLogoutSucceeded.map(_.flashing(
   *     "success" -> "You've been logged out"
   *   ))
   */
  def logout = Action.async { implicit request =>
    // do something...
    gotoLogoutSucceeded.map(_.flashing("success" -> "Successfully logged out"))
  }

  def signup = Action.async { implicit request =>
    formForSignup.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.signup(formWithErrors))),
      user => Future.successful(Redirect(routes.Application.index()).flashing("success" -> "Successfully signed up"))
    )
  }

}