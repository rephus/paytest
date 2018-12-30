package paytest.model


import org.specs2.mutable.{After, Before, BeforeAfter, Specification}
import slick.driver.H2Driver.api._
import slick.jdbc.meta._

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import paytest.DatabaseConfig
import paytest.factory._

class AccountTest extends Specification {

  val table = TableQuery[Accounts]
  val random = AccountFactory.random

  trait Context extends After {
    val dbName = s"test${util.Random.nextInt}"
    val db = Database.forURL(s"jdbc:h2:mem:$dbName", driver = "org.h2.Driver", keepAliveConnection = true)
    def after: Any = db.close()

  }

  "Creating a test schema should work" >> new Context {
    val numberOfTables = for {
      _ <- db.run(table.schema.create)
      numberOfTables <- db.run(MTable.getTables).map(_.size)
    } yield numberOfTables
    Await.result(numberOfTables, Duration.Inf) mustEqual (1)
  }

  "Schema should match our specification" >> new Context {
    table.schema.create.statements.toList === List(
      """create table "account" (
        |"id" VARCHAR NOT NULL PRIMARY KEY,
        |"account_name" VARCHAR NOT NULL,
        |"account_number" DECIMAL(21,2) NOT NULL,
        |"number_code" VARCHAR NOT NULL,
        |"account_type" INTEGER NOT NULL,
        |"address" VARCHAR NOT NULL,
        |"bank_id" INTEGER NOT NULL,
        |"bank_id_code" VARCHAR NOT NULL,
        |"name" VARCHAR NOT NULL)""".stripMargin.replaceAll("\n", "")
    )
  }

  "Inserting an account works" >> new Context {
    val insertCount = for {
      _ <- db.run(table.schema.create)
      insertCount <- db.run(table += random)
    } yield insertCount

    Await.result(insertCount, Duration.Inf) === 1
  }

  "Querying account table works" >> new Context {
    val account = random
    val resultsFuture = for {
      _ <- db.run(table.schema.create)
      _ <-  db.run(table += account)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size === 1
    results.head.id === account.id
    results.head.accountName === account.accountName
    results.head === account
  }
}
