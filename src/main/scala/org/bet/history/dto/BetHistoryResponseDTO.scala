package org.bet.history.dto

import org.bet.history.model.{Bet, BetHistory, Outcome}

trait BetHistoryResponseDTO

case class BetHistoryDTO(nodes: List[NodeDTO],
                         navigation: NavigationDTO) extends BetHistoryResponseDTO

object BetHistoryDTO {

  def from(betHistory: BetHistory, betAndOutcomePairs: List[(Bet, Outcome)]): BetHistoryDTO = {
    val nodes = betAndOutcomePairs.map {
      case (bet, outcome) =>
        NodeDTO(
          betId = bet.betId,
          payout = bet.payout,
          status = bet.status.toString,
          fixtureName = outcome.fixtureName,
          outcomeName = outcome.outcomeName,
          cursor = bet.cursor)
    }

    val navigation = NavigationDTO(
      hasNextPage = betHistory.hasNextPage,
      hasPreviousPage = betHistory.hasPreviousPage,
      firstCursor = betHistory.firstCursor getOrElse "none",
      lastCursor = betHistory.lastCursor getOrElse "none")

    BetHistoryDTO(nodes, navigation)
  }

}

case class NodeDTO(betId: String,
                   payout: Double,
                   status: String,
                   fixtureName: String,
                   outcomeName: String,
                   cursor: String)

case class NavigationDTO(hasNextPage: Boolean,
                         hasPreviousPage: Boolean,
                         firstCursor: String,
                         lastCursor: String)

case class ErrorDTO(errorMsg: String) extends BetHistoryResponseDTO
