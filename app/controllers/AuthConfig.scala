package controllers

import jp.t2v.lab.play2.auth._
import models.{ Session, User }
import play.api.Logger
import play.api.mvc.{ Result, RequestHeader }
import scala.annotation.tailrec
import scala.concurrent.{ Future, ExecutionContext }
import scala.reflect.ClassTag
import play.api.mvc.Controller
import scala.util.Random
import java.security.SecureRandom

class ActivateIdContainer extends IdContainer[String] {

  type Id = String
  private val random = new Random(new SecureRandom())
  private val prefix = "sessionId:"

  def startNewSession(userId: Id, timeoutInSeconds: Int): AuthenticityToken = {
    Session.deleteAllForUser(userId)
    val token = generate // generate token
    Session.set(userId)(prefix + token, token, timeoutInSeconds)
    token
  }

  def get(token: AuthenticityToken): Option[Id] = Session.get(prefix + token).map(_.userId)

  def remove(token: AuthenticityToken): Unit = {
    Session.unset(prefix + token)
  }

  def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int): Unit = {
    Session.setTimeout(prefix + token, timeoutInSeconds)
  }

  @tailrec
  private final def generate: AuthenticityToken = {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890_.!~*'()"
    val token = Iterator.continually(random.nextInt(table.size)).map(table).take(64).mkString
    if (get(token).isDefined) generate else token
  }

}

/**
 * Created by kindone on 15. 3. 28..
 */
trait AuthConfigImpl extends AuthConfig {
  self: Controller =>
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

  def signupFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.signupForm))

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
      case (models.Administrator, _)              => true
      case (models.NormalUser, models.NormalUser) => true
      case _                                      => false
    }
  }

  override lazy val idContainer: AsyncIdContainer[Id] = AsyncIdContainer(new ActivateIdContainer)

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
    cookieName = "WILLINGTODO_SESS_ID",
    cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )
}
