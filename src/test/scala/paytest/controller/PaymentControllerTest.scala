package paytest.controller

import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.specs2.mutable.Specification
import paytest.DatabaseConfig
import paytest.factory.PaymentFactory
import paytest.model._
import paytest.model.PaymentTest.{createSchema, insertPaymentAndAccounts}
import slick.driver.H2Driver.api._
import paytest.view.PaymentJsonProtocol._
import spray.http.StatusCodes
import spray.json._

class PaymentControllerTest extends Specification with Specs2RouteTest with PaymentController {
  // method actorRefFactory in trait HttpService of type => akka.actor.ActorRefFactoryç
  def actorRefFactory = system

  val randomPayment = PaymentFactory.random

  lazy val db = DatabaseConfig.db

  val payments = TableQuery[Payments]
  val accounts = TableQuery[Accounts]
  (payments.schema ++ accounts.schema).drop

  val dbInitialized = for {
    _ <- createSchema(db)
    dbInitialized <- insertPaymentAndAccounts(db, randomPayment)
  } yield dbInitialized

  Await.result(dbInitialized, Duration.Inf)

  "Payment controller" should {

    "return list of payments" in {
      val allQuery = db.run(payments.result)
      val allPayments = Await.result(allQuery, Duration.Inf)

      Get("/payment") ~> paymentRoutes ~> check {

        val paymentResults = responseAs[Seq[Payment]]
        paymentResults.size === allPayments.size
        paymentResults === allPayments
      }
    }
    "pagination: limit list of payments" in {
      val query = db.run(payments.result.head)
      val firstPayment = Await.result(query, Duration.Inf)

      Get("/payment?limit=1") ~> paymentRoutes ~> check {

        val paymentResults = responseAs[Seq[Payment]]
        paymentResults.size === 1
        paymentResults === Seq(firstPayment)
      }
    }
    "return payment by id" in {
      Get(s"/payment/${randomPayment.id.get}") ~> paymentRoutes ~> check {
        responseAs[String] must contain(randomPayment.id.get)
        responseAs[String] must contain(randomPayment.amount.toString.substring(0, 5))

        responseAs[Payment] === randomPayment
      }
    }

    "return 404 if payment does not exist" in {
      Get(s"/payment/missing-uuid") ~> sealRoute(paymentRoutes)  ~> check {
        status ===  NotFound
      }
    }
    "account endpoints are not handled" in {
      Get("/account") ~> paymentRoutes ~> check {
        handled must beFalse
      }
    }

    "delete a payment" in {
      val paymentToDelete = PaymentTest.random

      val ins = insertPaymentAndAccounts(db, paymentToDelete)
      Await.result(ins, Duration.Inf)

      // Check that it does exist before deletion
      val payment = db.run(payments.filter(_.id === paymentToDelete.id.get).result.headOption)
      Await.result(payment, Duration.Inf).get.id === paymentToDelete.id

      Delete(s"/payment/${paymentToDelete.id.get}") ~> paymentRoutes ~> check {
        responseAs[String] must contain("OK")

        // Check that it doesn't exist after deletion
        val payment = db.run(payments.filter(_.id === paymentToDelete.id.get).result.headOption)
        Await.result(payment, Duration.Inf) === None
      }
    }

    "create a payment via POST" in {
      //copy the same foreign keys as the previous random payment
      val newPayment = PaymentTest.random.copy(
        id = None,
        beneficiaryId = randomPayment.beneficiaryId,
        sponsorId =  randomPayment.beneficiaryId,
        debtorId = randomPayment.beneficiaryId)
      Post("/payment", newPayment) ~> paymentRoutes ~> check {
        status mustEqual StatusCodes.Created
        responseAs[Payment].copy(id=None) === newPayment

        val newPaymentId = responseAs[Payment].id.get
        val payment = db.run(payments.filter(_.id === newPaymentId).result.headOption)
        Await.result(payment, Duration.Inf).get.amount === newPayment.amount
        // check beneficiary foreign key

        val resultJson = responseAs[String].parseJson
        val account = resultJson.asJsObject.getFields("beneficiary").head.asJsObject.convertTo[Account]
        account.id.get === newPayment.beneficiaryId
      }
    }

    "invalid payment with invalid foreign keys should return an error" in {
      val newPayment = PaymentTest.random.copy(id = None,
        beneficiaryId = "not-valid-id",
        sponsorId = "not-valid-id",
        debtorId = "not-valid-id")
      Post("/payment", newPayment) ~> paymentRoutes ~> check {
        status mustEqual StatusCodes.BadRequest
        val result = responseAs[String]
        // We check that the response is returning a related error code (foreign key)
        // In the future, we should replace this with a proper code error (instead of returning the internal sql error)
        responseAs[String] must contain("Referential integrity constraint violation")
      }
    }

    "update a payment via PUT" in {
      //copy the same foreign keys as the previous random payment
      val paymentToUpdate = db.run(payments.filter(_.id === randomPayment.id.get).result.headOption)
      val paymentId = Await.result(paymentToUpdate, Duration.Inf).get.id.get

      val newPayment = randomPayment.copy(
        id = None,
        reference = "new reference")

      Put(s"/payment/${paymentId}", newPayment) ~> paymentRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[Payment].reference === "new reference"
        responseAs[Payment] === newPayment.copy(id=Some(paymentId))

        //Check in DB if record was updated
        val payment = db.run(payments.filter(_.id === paymentId).result.headOption)
        Await.result(payment, Duration.Inf).get.reference === "new reference"

      }
    }

    "Updating an invalid payment id via PUT should return an error" in {
      Put(s"/payment/not-valid", randomPayment) ~> paymentRoutes ~> check {
        status mustEqual StatusCodes.NotAcceptable
      }
    }

  }
}
