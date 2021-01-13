package org.bet.history.actors

import akka.actor.{Actor, Props}
import org.bet.history.actors.NotificationActor._
import org.bet.history.model.Notification

class NotificationActor extends Actor {

  override def receive: Receive = {
    case PrintNotification(notification) =>
      println(notification)
      sender() ! NotificationPrinted
  }

}

object NotificationActor {

  def props: Props =
    Props[NotificationActor]()

  case class PrintNotification(notification: Notification)

  case object NotificationPrinted

}
