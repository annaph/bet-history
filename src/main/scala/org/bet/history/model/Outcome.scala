package org.bet.history.model

case class Outcome(id: String,
                   fixtureName: String,
                   outcomeName: String)

case class OutcomeServiceError(msg: String,
                               throwable: Option[Throwable])
