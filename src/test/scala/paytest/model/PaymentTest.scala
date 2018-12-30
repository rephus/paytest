package paytest.model

import java.sql.Date
import java.util.UUID

import org.specs2.mutable.{After, Specification}
import slick.driver.H2Driver.api._
import slick.jdbc.meta._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Random
import PaymentTest._
import paytest.factory._

object PaymentTest {

  val table = TableQuery[Payments]
  val random = PaymentFactory.random

  def createSchema(db: Database) = {
    //required for foreign keys
    for {
      _ <- db.run(TableQuery[Accounts].schema.create)
      res <- db.run(table.schema.create)
    } yield res
  }

  def insertPaymentAndAccounts(db: Database, payment: Payment) = {
    //INsert payment and all foreign keys
    val accounts = TableQuery[Accounts]
    for {
      // Add foreign keys
      _ <- db.run(accounts += AccountFactory.random.copy(id = Some(payment.beneficiaryId)))
      _ <- db.run(accounts += AccountFactory.random.copy(id = Some(payment.sponsorId)))
      _ <- db.run(accounts += AccountFactory.random.copy(id = Some(payment.debtorId)))

      res <-  db.run(table += payment)
    } yield res
  }
}
class PaymentTest extends Specification {

  trait Context extends After {
    val dbName = s"test${util.Random.nextInt}"
    val db = Database.forURL(s"jdbc:h2:mem:$dbName", driver = "org.h2.Driver", keepAliveConnection = true)

    def after: Any = db.close()

  }

  "Creating a test schema should work" >> new Context {
    val numberOfTables = for {
      _ <- createSchema(db)
      numberOfTables <- db.run(MTable.getTables).map(_.size)
    } yield numberOfTables
    Await.result(numberOfTables, Duration.Inf) === 2
  }

  "Schema should match our specification" >> new Context {
    table.schema.create.statements.toList === List(
      """create table "payment" (
        |"id" VARCHAR NOT NULL PRIMARY KEY,
        |"amount" REAL NOT NULL,
        |"currency" VARCHAR NOT NULL,
        |"bearer_code" VARCHAR NOT NULL,
        |"sender_charges" VARCHAR NOT NULL,
        |"receiver_charges_amount" REAL NOT NULL,
        |"receiver_charges_currency" VARCHAR NOT NULL,
        |"end_to_end_reference" VARCHAR NOT NULL,
        |"numeric_reference" INTEGER NOT NULL,
        |"payment_id" DECIMAL(21,2) NOT NULL,
        |"payment_purpose" VARCHAR NOT NULL,
        |"payment_scheme" VARCHAR NOT NULL,
        |"payment_type" VARCHAR NOT NULL,
        |"processing_date" DATE NOT NULL,
        |"reference" VARCHAR NOT NULL,
        |"scheme_payment_subtype" VARCHAR NOT NULL,
        |"scheme_payment_type" VARCHAR NOT NULL,
        |"contract_reference" VARCHAR NOT NULL,
        |"exchange_rate" REAL NOT NULL,
        |"original_amount" REAL NOT NULL,
        |"original_currency" VARCHAR NOT NULL,
        |"beneficiary_id" VARCHAR NOT NULL,
        |"sponsor_id" VARCHAR NOT NULL,
        |"debtor_id" VARCHAR NOT NULL)""".stripMargin.replaceAll("\n", ""),
      """alter table "payment" add constraint "beneficiary_fk" foreign key("beneficiary_id") references "account"("id") on update SET NULL on delete SET NULL""",
      """alter table "payment" add constraint "debtor_fk" foreign key("debtor_id") references "account"("id") on update SET NULL on delete SET NULL""",
      """alter table "payment" add constraint "sponsor_fk" foreign key("sponsor_id") references "account"("id") on update SET NULL on delete SET NULL"""
    )
  }

  "Inserting an payment works" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insertPaymentAndAccounts(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size === 1

    val inserted = results.head
    inserted.id === payment.id
    inserted.amount === payment.amount
    inserted.currency === payment.currency

  }

  "Querying payment table works" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insertPaymentAndAccounts(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size === 1
    results.head.id === payment.id
    results.head.endToEndReference === payment.endToEndReference
    results.head === payment
  }

  "Payment should have FX" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insertPaymentAndAccounts(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size === 1
    results.head.id === payment.id
    results.head.fx.contractReference === payment.fx.contractReference
    results.head.fx === payment.fx
  }
  "Payment should have foreign keys" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insertPaymentAndAccounts(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size === 1
    results.head.id === payment.id
    results.head.endToEndReference === payment.endToEndReference
    results.head.beneficiaryId === payment.beneficiaryId

    val accounts = TableQuery[Accounts]
    val beneficiaryQuery = db.run(accounts.filter(_.id === payment.beneficiaryId).result.head)
    val beneficiary = Await.result(beneficiaryQuery, Duration.Inf)
    beneficiary.id === Some(payment.beneficiaryId)

  }

}
