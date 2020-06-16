// @GENERATOR:play-routes-compiler
// @SOURCE:/zaeem/Datumbrain/play-slick-scheduler-starter/conf/routes
// @DATE:Tue Jun 16 20:34:00 PKT 2020


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
