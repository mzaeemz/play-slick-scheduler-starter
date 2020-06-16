package v1.webfetcher

import javax.inject.{Inject, Singleton}
import java.io.File
import java.util

import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import play.api.libs.json._
import play.api.{Logger, MarkerContext}



import scala.concurrent.Future

final case class WebFetcherData(id: PostId, time_of_crawl: String, page_url: String, inbound_links: String, outbound_links: String, content: String)

final case class WebFetcherPostData(id: PostId, page_url: String, inbound_links: String)

class PostId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object PostId {
  def apply(raw: String): PostId = {
    require(raw != null)
    new PostId(Integer.parseInt(raw))
  }
}

class PostExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait WebFetcherRepository {
  def create(data: WebFetcherData)(implicit mc: MarkerContext): Future[PostId]

  def list()(implicit mc: MarkerContext): Future[Iterable[WebFetcherData]]

  def listPost()(implicit mc: MarkerContext): Future[Iterable[WebFetcherPostData]]
  
  def get(id: PostId)(implicit mc: MarkerContext): Future[Option[WebFetcherData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class WebFetcherRepositoryImpl @Inject()()(implicit ec: PostExecutionContext)
    extends WebFetcherRepository {

  private val logger = Logger(this.getClass)
  private var count = 1;
  var postList = new ListBuffer[WebFetcherData]()
  var postWebList = new ListBuffer[WebFetcherPostData]()

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[WebFetcherData]] = {
    Future {
      logger.trace(s"list: ")
      var outPath: String = "./output"
      val OUTDIR = new File(outPath)
      //OUTDIR.mkdir()
      var prevFiles: Array[File] = recursiveListFiles(OUTDIR)
      prevFiles = prevFiles.filter(f => """.*.json$""".r.findFirstIn(f.getName).isDefined)
      count=1
      //var seqJson = new util.ArrayList[JsValue]
      postList = new ListBuffer[WebFetcherData]()
      for (f <- prevFiles) {
        val source = scala.io.Source.fromFile(f.toString)
        val lines: String =
        try source.mkString
        finally source.close()
        var json = Json.parse(lines)
        var pd: WebFetcherData = new WebFetcherData(PostId(count.toString), ((json \ "time_of_crawl").get).toString, ((json \ "page_url").get).toString, ((json \ "inbound_links").get).toString, ((json \ "outbound_links").get).toString, ((json \ "content").get).toString)
        postList += pd
        count = count + 1
      }
      postList
    }
  }
  override def listPost()(
      implicit mc: MarkerContext): Future[Iterable[WebFetcherPostData]] = {
    Future {
      logger.trace(s"listPost: ")
      var outPath: String = "./output"
      val OUTDIR = new File(outPath)
      //OUTDIR.mkdir()
      var prevFiles: Array[File] = recursiveListFiles(OUTDIR)
      prevFiles = prevFiles.filter(f => """.*.json$""".r.findFirstIn(f.getName).isDefined)
      count=1
      //var seqJson = new util.ArrayList[JsValue]
      postWebList = new ListBuffer[WebFetcherPostData]()
      for (f <- prevFiles) {
        val source = scala.io.Source.fromFile(f.toString)
        val lines: String =
        try source.mkString
        finally source.close()
        var json = Json.parse(lines)
        var pd: WebFetcherPostData = new WebFetcherPostData(PostId(count.toString), ((json \ "page_url").get).toString, ((json \ "inbound_links").get).toString)
        postWebList += pd
        count = count + 1
      }
      postWebList
    }
  }


  override def get(id: PostId)(
      implicit mc: MarkerContext): Future[Option[WebFetcherData]] = {
    Future {
      logger.trace(s"get: id = $id")
      list()
      postList.find(post => post.id == id)
    }
  }
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
  def create(data: WebFetcherData)(implicit mc: MarkerContext): Future[PostId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
