package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.data._
import play.api.data.Forms._
import models.User
import views.html
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Application extends Controller with LoginLogout with AuthConfigImpl{

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  /** Your application's login form.  Alter it to fit your application */
  val loginForm = Form {
    mapping("username" -> email, "password" -> text)(User.authenticate)(_.map(u => (u.username, "")))
      .verifying("Invalid username or password", result => result.isDefined)
  }

  val signupForm = Form {
    mapping("username" -> email, "password" -> text, "passwordconfirm" -> text)(User.signup)(_.map(u => (u.username, "", "")))
      .verifying("Invalid username or password", result => result.isDefined)
  }


  /*
  def loginForm = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  def signupForm = Action {
    Ok(html.signup(signupForm))
  }
  */


  /**
   * Return the `gotoLoginSucceeded` method's result in the login action.
   *
   * Since the `gotoLoginSucceeded` returns `Future[Result]`,
   * you can add a procedure like the `gotoLogoutSucceeded`.
   */
  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.login(formWithErrors))),
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
    gotoLogoutSucceeded
  }

  def signup = Action.async { implicit request =>
      signupForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(html.signup(formWithErrors))),
        user => gotoLoginSucceeded(user.get.id.get)
      )
  }

  def quit = TODO

}