package org.bet.history.model

case class Notification(phoneNumber: String,
                        message: String) {

  override def toString: String =
    s"$message phoneNumber: '$phoneNumber'\n"

}

case class NotificationServiceError(msg: String,
                                    throwable: Option[Throwable])
