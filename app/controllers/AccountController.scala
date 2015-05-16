package controllers

import jp.t2v.lab.play2.auth.AuthElement
import jp.t2v.lab.play2.stackc.RequestWithAttributes
import models.{ NormalUser, User }

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Controller
import views.html

import scala.concurrent.Future

/**
 * Created by kindone on 15. 4. 5..
 */
object AccountController extends Controller with AuthElement with AuthConfigImpl {

  private def formForProfile(implicit request: RequestWithAttributes[_]) = Form {
    mapping("oldpassword" -> text,
      "password" -> tuple("main" -> text, "passwordconfirm" -> text)
        .verifying("password confirmation does not match", p => p._1 == p._2)
    )(User.update(loggedIn.id.get))(_.map(u => ("", ("", ""))))
      .verifying("password does not match", result => result.isDefined)
  }

  def index = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    Ok(views.html.profile(formForProfile))
  }

  def update = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    formForProfile.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.signup(formWithErrors))),
      user => Future.successful(Redirect(routes.Application.index()).flashing("success" -> "Successfully signed up"))
    )
  }

  def quit = TODO

}
