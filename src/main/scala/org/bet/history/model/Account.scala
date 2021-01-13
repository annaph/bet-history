package org.bet.history.model

case class Account(id: String,
                   name: String,
                   phoneNumber: String)

case class AccountServiceError(msg: String,
                               throwable: Option[Throwable])
