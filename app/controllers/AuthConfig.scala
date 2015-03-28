package controllers

import jp.t2v.lab.play2.auth._
import models.User
import play.api.mvc.{Result, RequestHeader}
import play.mvc.Results._
import scala.concurrent.{Future, ExecutionContext}
import scala.reflect.ClassTag
import play.api.mvc.Controller

/**
 * Created by kindone on 15. 3. 28..
 */
trait AuthConfigImpl extends AuthConfig {
  self:Controller =>
  type Id = String
  type User = models.User.Frozen
  type Authority = models.Role
  val idTag: ClassTag[Id] = scala.reflect.classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600


  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = Future.successful {
    User.find(id).map(_.frozen)
  }

  /**
   * Where to redirect the user after a successful login.
   */
  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  /**
   * Where to redirect the user after logging out
   */
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  /**
   * If the user is not logged in and tries to access a protected resource then redirect them as follows:
   */
  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("no permission"))
  }
  
  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = ???


  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (user.role, authority) match {
      case (models.Administrator, _) => true
      case (models.NormalUser, models.NormalUser) => true
      case _ => false
    }
  }

  /**
   * (Optional)
   * You can custom SessionID Token handler.
   * Default implementation use Cookie.
   */
  override lazy val tokenAccessor = new CookieTokenAccessor(
    /*
     * Whether use the secure option or not use it in the cookie.
     * However default is false, I strongly recommend using true in a production.
     */
    cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )
}
