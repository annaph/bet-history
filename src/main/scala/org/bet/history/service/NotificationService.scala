package org.bet.history.service

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern._
import org.bet.history.actors.NotificationActor
import org.bet.history.model.{Notification, NotificationServiceError}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait NotificationService {

  def send(notification: Notification): Future[Either[NotificationServiceError, Unit]]

}

class NotificationServiceImpl(notificationActor: ActorSelection) extends NotificationService {

  override def send(notification: Notification): Future[Either[NotificationServiceError, Unit]] = {
    val result = notificationActor ? NotificationActor.PrintNotification(notification)

    result.collect {
      case NotificationActor.NotificationPrinted =>
        Right(())
    }
  }

}

object NotificationServiceImpl {

  def apply(actorSystem: ActorSystem): NotificationServiceImpl = {
    val notificationActor = actorSystem actorSelection "/user/bet-history-parent-actor/notification-actor"
    new NotificationServiceImpl(notificationActor)
  }

}
