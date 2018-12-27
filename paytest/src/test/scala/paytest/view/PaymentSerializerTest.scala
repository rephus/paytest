package paytest.view

import org.specs2.mutable.Specification
import paytest.model.{Account, AccountTest, Fx, PaymentTest}
import spray.json._
import paytest.view.PaymentJsonProtocol._


class PaymentSerializerTest extends Specification {

  "Payment" should {

    "should transform and parse Fx to JSON" in {

      val fx = PaymentTest.random.fx
      val json = fx.toJson

      json.asJsObject.getFields("contract_reference").head.convertTo[String] === fx.contractReference
      json.asJsObject.getFields("original_amount").head.convertTo[Float] === fx.originalAmount
      json.asJsObject.getFields("original_currency").head.convertTo[String] === fx.originalCurrency

      PaymentJsonProtocol.FxFormat.read(json) === fx

    }
    "should transform to JSON" in {

      val payment = PaymentTest.random
      val json = payment.toJson

      json.asJsObject.getFields("id").head.convertTo[String] === payment.id.get
      json.asJsObject.getFields("beneficiary_id").head.convertTo[String] === payment.beneficiaryId
      json.asJsObject.getFields("amount").head.convertTo[Float] === payment.amount

      json.asJsObject.getFields("fx").head.asJsObject.convertTo[Fx] === payment.fx
      json.asJsObject.getFields("currency").head.convertTo[String] === payment.currency

      json.asJsObject.getFields("receiver_charges_amount").head.convertTo[Float] === payment.receiverChargesAmount

    }

    "should transform foreign keys to JSON" in {

      val payment = PaymentTest.random
      payment.beneficiary = Some(AccountTest.random)
      val json = payment.toJson
      json.asJsObject.getFields("beneficiary_id").head.convertTo[String] === payment.beneficiaryId
      json.asJsObject.getFields("beneficiary").head.asJsObject.convertTo[Account] === payment.beneficiary.get
      json.asJsObject.getFields("debtor").head.toString === "null"


    }

    "should parse from JSON" in {

      val payment = PaymentTest.random
      val json = payment.toJson

      PaymentJsonProtocol.PaymentFormat.read(json) === payment
    }
    "should transform sender_charges" in {

      val json = """[{"amount":35.66,"currency":"GBP"},{"amount":13.37,"currency":"USD"}]"""
      val charges = "35.66 GBP;13.37 USD"

       PaymentJsonProtocol.senderCharges(charges).toString === json
    }

    "should parse sender_charges" in {

      val json = """[{"amount":35.66,"currency":"GBP"},{"amount":13.37,"currency":"USD"}]"""
      val charges = "35.66 GBP;13.37 USD"

      PaymentJsonProtocol.parseSenderCharges(json.parseJson) === charges
    }

  }

}
