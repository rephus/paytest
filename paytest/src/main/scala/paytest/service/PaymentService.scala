package paytest.service

import java.util.UUID

import org.slf4j.LoggerFactory
import paytest.{Boot, DatabaseConfig}
import paytest.model.{Payment, Payments}

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.PostgresDriver.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object PaymentService {
  private val logger = LoggerFactory.getLogger(this.getClass)

  var db = DatabaseConfig.db
  val payments = TableQuery[Payments]

  def delete(id: String) = db.run {
    payments.filter(_.id === id).delete
  }
  def getAll(limit: Int = 10, from: Int = 0): Future[Seq[Payment]] = db.run {
    logger.info("getAll")
    payments.drop(from).take(limit).result
  }

  def get(id: String): Future[Option[Payment]] = db.run {

    payments.filter(_.id === id).result.headOption
  }
  def update(id: String, payment: Payment) = db.run {

    for {
      _ <- payments.filter(_.id === id).update(payment.copy(id=Some(id)))
      payment <- payments.filter(_.id === id).result.headOption

    } yield payment

  }

  def add(payment: Payment)= db.run {
    val insertPayment = payment.copy(id=Some(UUID.randomUUID().toString) )
    for {
      _ <- payments += insertPayment
      payment <- payments.filter(_.id === insertPayment.id).result.headOption

    } yield payment
  }

  /**
    * Take a single payment and get all the foreing keys,
    * then populate the variables and return the payment
    * @param payment payment to be populated
    * @return payment with foreign keys variables populated
    */
  def populatePayment(payment: Payment) =  {
    val foreignQueries= for {
      beneficiary <- AccountService.get(payment.beneficiaryId)
      sponsor <- AccountService.get(payment.sponsorId)
      debtor <- AccountService.get(payment.debtorId)
    } yield Map(
      "beneficiary" -> beneficiary,
      "sponsor" ->sponsor,
      "debtor" -> debtor)

    val foreignObjects = Await.result(foreignQueries, Duration.Inf)
    payment.beneficiary = foreignObjects.get("beneficiary")
    payment.sponsor =foreignObjects.get("sponsor")
    payment.debtor = foreignObjects.get("debtor")
    payment
  }

  /**
    * Take a list of payments and get all the foreing keys for all itemns
    * on a single wait, then populate the variables and return the list of payments
    * @param allPayments list of payments
    * @return list of payments with filled foreign keys as variables
    */
  def populatePayments(allPayments: Seq[Payment]): Seq[Payment] = {
    val foreignQueries=  allPayments.map( payment => {
      for {
        beneficiary <- AccountService.get(payment.beneficiaryId)
        sponsor <- AccountService.get(payment.sponsorId)
        debtor <- AccountService.get(payment.debtorId)
      } yield Map(payment.id.get -> Map(
        "beneficiary" -> beneficiary,
        "sponsor" ->sponsor,
        "debtor" -> debtor)
      )
    })

    val futureOfList = Future.sequence(foreignQueries)

    val foreignObjects = Await.result(futureOfList, Duration.Inf)
    val accountMapping = foreignObjects reduce (_ ++ _)

    allPayments.map( payment => {
      payment.beneficiary = accountMapping(payment.id.get).get("beneficiary")
      payment.sponsor = accountMapping(payment.id.get).get("sponsor")
      payment.debtor = accountMapping(payment.id.get).get("debtor")
      payment
    })

  }

}