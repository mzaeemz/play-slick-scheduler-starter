import javax.inject._

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import v1.webfetcher._
import services.WebFetcherDaemon

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {
  override def configure() = {
    bind[WebFetcherRepository].to[WebFetcherRepositoryImpl].in[Singleton]
    bind(classOf[WebFetcherDaemon]).asEagerSingleton()
  
  }
}
