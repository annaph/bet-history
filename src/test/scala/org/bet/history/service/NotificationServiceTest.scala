package org.bet.history.service

import org.bet.history.Utils.actorSystem
import org.bet.history.model.Notification
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class NotificationServiceTest extends AnyFlatSpec with Matchers {

  private val notificationService = NotificationServiceImpl(actorSystem())

  "send" should "print notification to stdout" in {
    // given
    val notification = Notification(phoneNumber = "555-0152", message = "Some message")

    // when
    val actual: Unit = Await.result(notificationService send notification, 3.seconds).toOption.get

    //then
    actual shouldBe a[Unit]
  }

}
