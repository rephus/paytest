package paytest.model

import java.sql.Date
import slick.driver.H2Driver.api._

case class Fx(contractReference: String, exchangeRate: Float, originalAmount: Float, originalCurrency: String)

case class Payment(id: Option[String] = None, amount: Float, currency: String, bearerCode: String, senderCharges: String,
                   receiverChargesAmount: Float, receiverChargesCurrency: String, endToEndReference: String, numericReference: Int,
                   paymentId: BigDecimal, paymentPurpose: String, paymentScheme: String, paymentType: String, processingDate: Date,
                   reference: String, schemePaymentSubtype: String, schemePaymentType: String,
                   fx: Fx,
                   //references
                   beneficiaryId: String, sponsorId: String, debtorId: String) {
  var beneficiary: Option[Account] = None
  var sponsor: Option[Account] = None
  var debtor: Option[Account] = None

}


class Payments(tag: Tag) extends Table[Payment](tag, "payment") {

  val accounts = TableQuery[Accounts]

  def id = column[String]("id", O.PrimaryKey)

  def amount = column[Float]("amount")
  def currency =  column[String]("currency")
  def bearerCode =  column[String]("bearer_code")
  def senderCharges =  column[String]("sender_charges")

  def receiverChargesAmount =  column[Float]("receiver_charges_amount")
  def receiverChargesCurrency =  column[String]("receiver_charges_currency")
  def endToEndReference =  column[String]("end_to_end_reference")
  def numericReference =  column[Int]("numeric_reference")

  def paymentId =  column[BigDecimal]("payment_id")
  def paymentPurpose =  column[String]("payment_purpose")
  def paymentScheme =  column[String]("payment_scheme")
  def paymentType =  column[String]("payment_type")
  def processingDate =  column[Date]("processing_date")

  def reference =  column[String]("reference")
  def schemePaymentSubtype =  column[String]("scheme_payment_subtype")
  def schemePaymentType =  column[String]("scheme_payment_type")

  def fx = (contractReference, exchangeRate, originalAmount, originalCurrency)
  def contractReference =  column[String]("contract_reference")
  def exchangeRate =  column[Float]("exchange_rate")
  def originalAmount =  column[Float]("original_amount")
  def originalCurrency =  column[String]("original_currency")

  def beneficiaryId = column[String]("beneficiary_id")
  def beneficiary = foreignKey("beneficiary_fk", beneficiaryId, accounts)(_.id, onUpdate=ForeignKeyAction.SetNull, onDelete=ForeignKeyAction.SetNull)
  def sponsorId = column[String]("sponsor_id")
  def sponsor = foreignKey("sponsor_fk", sponsorId, accounts)(_.id, onUpdate=ForeignKeyAction.SetNull, onDelete=ForeignKeyAction.SetNull)
  def debtorId = column[String]("debtor_id")
  def debtor = foreignKey("debtor_fk", debtorId, accounts)(_.id, onUpdate=ForeignKeyAction.SetNull, onDelete=ForeignKeyAction.SetNull)

  // Fx is a class inside payment, we do it this way to avoid the 22 max field limit on Slick
  val fxProjection = fx <> (Fx.tupled, Fx.unapply)

  def * = (id.?, amount, currency, bearerCode, senderCharges,
    receiverChargesAmount, receiverChargesCurrency, endToEndReference, numericReference,
    paymentId, paymentPurpose, paymentScheme, paymentType, processingDate,
    reference, schemePaymentSubtype, schemePaymentType,
    fxProjection, // contractReference, exchangeRange, originalAmount, originalCurrency,
    beneficiaryId, sponsorId, debtorId) <> (Payment.tupled, Payment.unapply)
}