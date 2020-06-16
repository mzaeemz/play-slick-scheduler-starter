package v1.webfetcher

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._



import scala.concurrent.{ExecutionContext, Future}

case class PostFormInput(time_of_crawl: String, page_url: String, inbound_links: String, outbound_links: String, content: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class WebFetcherController @Inject()(cc: WebFetcherControllerComponents)(
    implicit ec: ExecutionContext)
    extends WebFetcherBaseController(cc) {

  private val logger = Logger(getClass)
  
  private val form: Form[PostFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "time_of_crawl" -> text,
        "page_url" -> text,
        "inbound_links" -> text,
        "outbound_links" -> text,
        "content" -> text
      )(PostFormInput.apply)(PostFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("index: ")
    postResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("process: ")
    postResourceHandler.findPost.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def show(id: String): Action[AnyContent] = PostAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      postResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  private def processJsonPost[A]()(
      implicit request: PostRequest[A]): Future[Result] = {
    def failure(badForm: Form[PostFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PostFormInput) = {
      postResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    
    form.bindFromRequest().fold(failure, success)
  }
}
