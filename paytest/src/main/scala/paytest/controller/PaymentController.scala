package paytest.controller

import paytest.model.Payment
import paytest.service.PaymentService
import spray.routing._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success}
import spray.http.StatusCodes._
import spray.http.MediaTypes._

import scala.concurrent.ExecutionContext.Implicits.global
import paytest.view.PaymentJsonProtocol._

trait PaymentController extends HttpService with DefaultJsonProtocol with SprayJsonSupport {

  val paymentRoutes: Route = getById ~ getAllRoute ~deleteRoute ~ createRoute ~ updateRoute

  def getById = path("payment" / Rest) { paymentId =>
    get {
      respondWithMediaType(`application/json`) {

        onComplete(PaymentService.get(paymentId)) {

          case Success(Some(payment)) => {
            val paymentWithAccount = PaymentService.populatePayment(payment)
            complete(payment)
          }
          case Success(None) => complete(NotFound, "Payment not found")
          case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }
  def getAllRoute: Route = path("payment" ) {
    get {
      parameters('limit.?, 'from.?) { (limit, from) =>
      respondWithMediaType(`application/json`) {
        onComplete(PaymentService.getAll(limit=limit.getOrElse("10").toInt, from=from.getOrElse("0").toInt)) {
          case Success(allPayments) => {
            val paymentsWithAccounts = PaymentService.populatePayments(allPayments)
            complete(paymentsWithAccounts)
          }
          case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
    }
  }
  def createRoute: Route = path("payment") {
    post {
      entity(as[Payment]) { payment =>
        respondWithMediaType(`application/json`) {
          onComplete(PaymentService.add(payment)) {
            case Success(Some(newPayment)) => complete(Created,newPayment)
            case Success(None) => complete(NotAcceptable, "Invalid payment")
            case Failure(ex) => {
              complete(BadRequest, s"An error occurred: ${ex.getMessage}")
            }

          }
        }
      }
    }
  }
  def updateRoute: Route = path("payment"/ Rest) { paymentId =>
    put {
      entity(as[Payment]) { payment =>
        respondWithMediaType(`application/json`) {
          onComplete(PaymentService.update(paymentId, payment)) {
            case Success(Some(payment)) => complete(OK, payment)
            case Success(None) => complete(NotAcceptable, "Invalid payment")
            case Failure(ex) =>  complete(BadRequest, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
  }

  def deleteRoute = path("payment" / Rest) { paymentId =>
    delete {
      respondWithMediaType(`application/json`) {

        onComplete(PaymentService.delete(paymentId)) {
          case Success(ok) => complete(OK)
          case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }
}