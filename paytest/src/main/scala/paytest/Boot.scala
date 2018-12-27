package paytest

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import paytest.controller.SprayActor
import paytest.model.{Accounts, Payments}

import scala.concurrent.duration._
import slick.driver.PostgresDriver.api._


object Boot extends App {

  private val logger = LoggerFactory.getLogger(this.getClass)
  // DB setup
  val db = DatabaseConfig.db

  val conf = ConfigFactory.load()

  val url = conf.getString("db.url")
  logger.info(s"Migrating database on url ${url}")
  val flyway = new Flyway()
  flyway.setDataSource(url, conf.getString("db.user"), conf.getString("db.password"))
  flyway.migrate()

  implicit val system = ActorSystem("on-spray-can")

  // create and start our service based on akka actor models that contains all endpoints for our project
  val service = system.actorOf(Props[SprayActor], "paytest-service")

  implicit val timeout = Timeout(5.seconds)
  val port = conf.getInt("spray.port")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = port)
}
