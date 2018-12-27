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

object PaymentTest {

  def randomCurrency = Random.shuffle(List("GBP", "EUR", "USD")).head
  def randomString = Random.alphanumeric.take(Random.nextInt(10)).mkString

  def random: Payment = {
    Payment(
      id = Some(UUID.randomUUID().toString),
      amount = Random.nextFloat,
      currency = randomCurrency,
      bearerCode = randomString,
      senderCharges = s"${Random.nextFloat} $randomCurrency;${Random.nextFloat} $randomCurrency"  ,
      receiverChargesAmount = Random.nextFloat,
      receiverChargesCurrency =  randomCurrency,
      endToEndReference = randomString,
      numericReference = Random.nextInt(),
      paymentId = BigDecimal.apply(Random.nextInt),
      paymentPurpose = randomString,
      paymentScheme = randomString,
      paymentType = randomString,
      processingDate = Date.valueOf("2018-01-01"),
      reference = randomString,
      schemePaymentSubtype = randomString,
      schemePaymentType = randomString,
      //fx
      fx=Fx(
        contractReference= randomString,
        exchangeRate= Random.nextFloat,
        originalAmount= Random.nextFloat,
        originalCurrency= randomString),
      beneficiaryId= UUID.randomUUID().toString,
      sponsorId= UUID.randomUUID().toString,
      debtorId= UUID.randomUUID().toString)
  }
  val table = TableQuery[Payments]

  def createSchema(db: Database) = {
    //required for foreign keys
    for {
      _ <- db.run(TableQuery[Accounts].schema.create)

      res <- db.run(table.schema.create)
    } yield res
  }

  def insert(db: Database, payment: Payment) = {
    //INsert payment and all foreign keys
    val accounts = TableQuery[Accounts]
    for {
      // Add foreign keys
      _ <- db.run(accounts += AccountTest.random.copy(id = Some(payment.beneficiaryId)))
      _ <- db.run(accounts += AccountTest.random.copy(id = Some(payment.sponsorId)))
      _ <- db.run(accounts += AccountTest.random.copy(id = Some(payment.debtorId)))

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
    Await.result(numberOfTables, Duration.Inf) mustEqual (2)
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
    val insertCount = for {
      _ <- createSchema(db)
      insertCount <- insert(db, random)
    } yield insertCount

    Await.result(insertCount, Duration.apply(5, "second")) must beEqualTo(1)
  }

  "Querying payment table works" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insert(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size must beEqualTo(1)
    results.head.id must beEqualTo(payment.id)
    results.head.endToEndReference must beEqualTo(payment.endToEndReference)
    results.head === payment
  }

  "Check payment foreign FX" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insert(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size must beEqualTo(1)
    results.head.id must beEqualTo(payment.id)
    results.head.fx.contractReference === payment.fx.contractReference
    results.head.fx === payment.fx
  }
  "Check payment foreign keys" >> new Context {
    val payment = random
    val resultsFuture = for {
      _ <- createSchema(db)
      _ <-  insert(db, payment)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size must beEqualTo(1)
    results.head.id must beEqualTo(payment.id)
    results.head.endToEndReference must beEqualTo(payment.endToEndReference)
    results.head.beneficiaryId === payment.beneficiaryId

    val accounts = TableQuery[Accounts]
    val beneficiaryQuery = db.run(accounts.filter(_.id === payment.beneficiaryId).result.head)
    val beneficiary = Await.result(beneficiaryQuery, Duration.Inf)
    beneficiary.id === Some(payment.beneficiaryId)

  }

}
