package org.bet.history.model

case class BetHistory(bets: List[Bet],
                      hasNextPage: Boolean,
                      hasPreviousPage: Boolean,
                      firstCursor: Option[String],
                      lastCursor: Option[String])

