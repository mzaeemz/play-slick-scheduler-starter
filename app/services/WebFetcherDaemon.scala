package services

import javax.inject._

import sys.process._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import akka.actor.{Actor, ActorRef, Cancellable, Props}

import play.api.inject.ApplicationLifecycle
import play.api._
import scala.language.postfixOps
import com.datumbrain.webfetcher


@Singleton
class WebFetcherDaemon @Inject() (app: Application, appLifecycle: ApplicationLifecycle) {
  val Tick = "tick"
  import com.typesafe.config.ConfigFactory
  val url = ConfigFactory.load().getString("web.fetcher.url")
  //val cmnd = "java -jar webfetcher-0.0.1.jar " + url
  class FetchActor extends Actor {
    def receive = {
      case Tick => new webfetcher.MainClass(url)
    }
  }
  val system = akka.actor.ActorSystem("system")
  val fetchActor: ActorRef = system.actorOf(Props(classOf[FetchActor], this))
  val cancellable: Cancellable = system.scheduler.scheduleWithFixedDelay(Duration.Zero, 2.minutes, fetchActor, Tick)(ExecutionContext.global)
  appLifecycle.addStopHook { () =>
    cancellable.cancel()
    Future.successful(())
  }
}