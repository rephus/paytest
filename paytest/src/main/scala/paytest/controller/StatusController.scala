package paytest.controller

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.routing._
import spray.http.StatusCodes._

trait StatusController extends HttpService {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val conf = ConfigFactory.load()

  val statusRoutes =
    path("") {
      get {
        complete(Map(
          "status" -> "ok",
          "environment" -> conf.getString("env"),
          "name" -> conf.getString("service") )
        )
      }
    } ~ path("ping" ) {
      get {
        complete("pong")
      }

    }
}
