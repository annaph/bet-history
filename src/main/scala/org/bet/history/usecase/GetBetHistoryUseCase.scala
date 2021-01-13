package org.bet.history.usecase

import org.bet.history.dto.{BetHistoryDTO, BetHistoryResponseDTO, ErrorDTO}
import org.bet.history.model._
import org.bet.history.service.{BetService, OutcomeService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GetBetHistoryUseCase(betService: BetService, outcomeService: OutcomeService) {

  def betHistory(accountId: String,
                 first: String,
                 after: Option[String],
                 before: Option[String]): Future[BetHistoryResponseDTO] =
    (first, after, before) match {
      case (first, _, _) if !first.matches("\\d+") || first.toInt < 1 =>
        Future successful ErrorDTO(errorMsg = "Argument 'first' must be positive integer!")

      case (_, Some(_), Some(_)) =>
        Future successful ErrorDTO(errorMsg = "Both arguments 'after' and 'before' cannot be specified!")

      case (first, None, None) =>
        val betHistory = getBetHistory(accountId, first.toInt)
        createBetHistoryResponseDTO(betHistory)

      case (first, Some(after), None) =>
        val betHistory = getBetHistoryFrom(accountId, after, first.toInt)
        createBetHistoryResponseDTO(betHistory)

      case (first, None, Some(before)) =>
        val betHistory = getBetHistoryTo(accountId, before, first.toInt)
        createBetHistoryResponseDTO(betHistory)
    }

  private def createBetHistoryResponseDTO(betHistory: Future[BetHistory]): Future[BetHistoryResponseDTO] =
    for {
      bHistory <- betHistory
      betAndOutcomePairs <- getBetAndOutcomePairs(bHistory.bets)
    } yield bHistory.bets match {
      case Nil =>
        ErrorDTO(errorMsg = s"No bet history could be found!")
      case _ =>
        BetHistoryDTO.from(bHistory, betAndOutcomePairs)
    }

  private def getBetAndOutcomePairs(bets: List[Bet]): Future[List[(Bet, Outcome)]] = {
    val betAndOutcomePairs = for (bet <- bets) yield getOutcome(bet.outcomeId).map(bet -> _)
    Future sequence betAndOutcomePairs
  }

  private def getOutcome(outcomeId: String): Future[Outcome] = {
    outcomeService.getOutcome(outcomeId).map {
      case Right(outcome) =>
        outcome
      case Left(serviceError) =>
        throw GetBetHistoryException(msg = serviceError.msg, cause = serviceError.throwable)
    }
  }

  private def getBetHistory(accountId: String, first: Int): Future[BetHistory] =
    extractBetServiceResponse {
      betService.getBetHistory(accountId, first)
    }

  private def getBetHistoryFrom(accountId: String, after: String, first: Int): Future[BetHistory] =
    extractBetServiceResponse {
      betService.getBetHistoryFrom(accountId, after, first)
    }

  private def extractBetServiceResponse(betServiceResponse: Future[Either[BetServiceError, BetHistory]]): Future[BetHistory] =
    betServiceResponse.map {
      case Right(betHistory) =>
        betHistory
      case Left(serviceError) =>
        throw GetBetHistoryException(msg = serviceError.msg, cause = serviceError.throwable)
    }

  private def getBetHistoryTo(accountId: String, before: String, first: Int): Future[BetHistory] =
    extractBetServiceResponse {
      betService.getBetHistoryTo(accountId, before, first)
    }

}

object GetBetHistoryUseCase {

  def apply(betService: BetService, outcomeService: OutcomeService): GetBetHistoryUseCase =
    new GetBetHistoryUseCase(betService, outcomeService)

}

case class GetBetHistoryException(msg: String, cause: Option[Throwable]) extends Exception
