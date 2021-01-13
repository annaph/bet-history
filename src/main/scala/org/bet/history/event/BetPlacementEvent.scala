package org.bet.history.event

import java.time.Instant

import org.bet.history.StringOps

trait BetPlacementEvent

case class BetPlaced(betId: String,
                     accountId: String,
                     outcomeId: String,
                     payout: Double,
                     timestamp: Instant) extends BetPlacementEvent

object BetPlaced {

  def from(betId: String, accountId: String, outcomeId: String, payout: String, timestamp: String): BetPlaced =
    BetPlaced(betId, accountId, outcomeId, payout.toDouble, timestamp.toInstant)

}
