package org.bet.history.event

import java.time.Instant

import org.bet.history.StringOps

trait BetSettlementEvent

case class BetWon(betId: String,
                  timestamp: Instant) extends BetSettlementEvent

object BetWon {

  def from(betId: String, timestamp: String): BetWon =
    BetWon(betId, timestamp.toInstant)

}

case class BetLost(betId: String,
                   timestamp: Instant) extends BetSettlementEvent

object BetLost {

  def from(betId: String, timestamp: String): BetLost =
    BetLost(betId, timestamp.toInstant)

}
