package paytest.factory

import java.sql.Date
import java.util.UUID

import paytest.model.{Fx, Payment}

import scala.util.Random

object PaymentFactory {
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
}