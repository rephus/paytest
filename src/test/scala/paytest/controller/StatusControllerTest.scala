package paytest.controller

import org.specs2.mutable.Specification

import spray.testkit.Specs2RouteTest
import spray.json._

class StatusControllerTest extends Specification with Specs2RouteTest with StatusController {
  def actorRefFactory = system

  "Status controller" should {

    "return ping" in {
      Get(s"/ping") ~> statusRoutes ~> check {
        responseAs[String] === "pong"
      }
    }

    "return application status" in {
      Get(s"/") ~> statusRoutes ~> check {

        responseAs[String] ===
          """{
         |  "status": "ok",
         |  "environment": "test",
         |  "name": "paytest"
         |}""".stripMargin
      }
    }


  }
}
