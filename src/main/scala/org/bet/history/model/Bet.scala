package org.bet.history.model

import java.time.Instant
import java.util.UUID

import org.bet.history.StringOps

case class Bet(betId: String,
               accountId: String,
               outcomeId: String,
               payout: Double,
               status: BetStatus,
               modified: Instant) {

  val cursor: String = {
    val uuid = new UUID(betId.hashCode, (betId * 2).hashCode)
    uuid.toString.takeRight(8)
  }

  override def equals(that: Any): Boolean = that match {
    case that: Bet =>
      this.canEqual(that) && this.betId == that.betId
    case _ =>
      false
  }

  override def canEqual(that: Any): Boolean =
    that.isInstanceOf[Bet]

  override def hashCode(): Int =
    betId.hashCode

  override def toString: String =
    s"Bet(betId: '$betId', accountId: '$accountId', outcomeId: '$outcomeId', " +
      s"payout: '$payout', status: '$status', modified: '$modified', " +
      s"cursor: '$cursor')"

}

object Bet {

  def from(betId: String, accountId: String, outcomeId: String, payout: String, modified: String): Bet =
    Bet(betId, accountId, outcomeId, payout.toDouble, BetStatus.Open, modified.toInstant)

}

case class BetServiceError(msg: String,
                           throwable: Option[Throwable])
