package paytest


import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import slick.driver.{H2Driver, JdbcProfile, PostgresDriver}
import slick.jdbc.JdbcBackend._

//Based on play-slick driver loader
object DatabaseConfig {
  private val logger = LoggerFactory.getLogger(this.getClass)

  var db = Database.forConfig("db")
  val conf = ConfigFactory.load()
  val driver = conf.getString("db.driver")
  logger.info("Loading DB with driver " + driver)
  lazy val profile: JdbcProfile = driver match {
    case "org.postgresql.Driver" => PostgresDriver
    case "org.h2.Driver" => H2Driver
  }
}