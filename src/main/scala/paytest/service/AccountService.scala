package paytest.service


import paytest.DatabaseConfig
import paytest.model.{Account, Accounts}
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountService {

  var db = DatabaseConfig.db
  val accounts = TableQuery[Accounts]

  def get(id: String): Future[Account] = db.run {
    accounts.filter(_.id === id).result.head
  }
}