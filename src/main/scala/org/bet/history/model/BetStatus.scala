package org.bet.history.model

sealed trait BetStatus

object BetStatus {

  case object Open extends BetStatus

  case object Won extends BetStatus

  case object Lost extends BetStatus

}
