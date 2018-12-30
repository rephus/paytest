package paytest.controller

import akka.actor.Actor

class SprayActor extends Actor with PaymentController with StatusController {
  def actorRefFactory = context
  def receive = runRoute(paymentRoutes ~ statusRoutes)
}
