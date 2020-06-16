package v1.webfetcher

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
case class WebFetcherResource(id: String, link: String, time_of_crawl: String, page_url: String, inbound_links: String, outbound_links: String, content: String)
case class WebFetcherPostResource(id: String, page_url: String, inbound_links: String)

object WebFetcherResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
    implicit val format: Format[WebFetcherResource] = Json.format
}

object WebFetcherPostResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
    implicit val format: Format[WebFetcherPostResource] = Json.format
}

/**
  * Controls access to the backend data, returning [[PostResource]]
  */
class WebFetcherResourceHandler @Inject()(
    routerProvider: Provider[WebFetcherRouter],
    webfetcherRepository: WebFetcherRepository)(implicit ec: ExecutionContext) {

  def create(postInput: PostFormInput)(
      implicit mc: MarkerContext): Future[WebFetcherResource] = {
    val data = WebFetcherData(PostId("69"), postInput.time_of_crawl, postInput.page_url, postInput.inbound_links, postInput.outbound_links, postInput.content)
    // We don't actually create the post, so return what we have
    webfetcherRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[WebFetcherResource]] = {
    val postFuture = webfetcherRepository.get(PostId(id))
    postFuture.map { maybeWebFetcherData =>
      maybeWebFetcherData.map { data =>
        createPostResource(data)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[WebFetcherResource]] = {
    webfetcherRepository.list().map { dataList =>
      dataList.map(data => createPostResource(data))
    }
  }
  
  def findPost(implicit mc: MarkerContext): Future[Iterable[WebFetcherPostResource]] = {
    webfetcherRepository.listPost().map { dataPostList =>
      dataPostList.map(data => createWebPostResource(data))
    }
  }

  private def createPostResource(p: WebFetcherData): WebFetcherResource = {
    WebFetcherResource(p.id.toString, routerProvider.get.link(p.id), p.time_of_crawl, p.page_url, p.inbound_links, p.outbound_links, p.content)
  }
  private def createWebPostResource(p: WebFetcherPostData): WebFetcherPostResource = {
    WebFetcherPostResource(p.id.toString, p.page_url, p.inbound_links)
  }
}
