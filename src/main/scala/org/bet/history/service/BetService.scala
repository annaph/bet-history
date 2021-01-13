package org.bet.history.service

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern._
import org.bet.history.actors.BetActor
import org.bet.history.actors.BetActor.{BetDoesNotExist, OldBetUpdateData}
import org.bet.history.model.{Bet, BetHistory, BetServiceError}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BetService {

  def getBet(betId: String): Future[Either[BetServiceError, Bet]]

  def getBetHistory(accountId: String, first: Int): Future[Either[BetServiceError, BetHistory]]

  def getBetHistoryFrom(accountId: String, cursor: String, first: Int): Future[Either[BetServiceError, BetHistory]]

  def getBetHistoryTo(accountId: String, cursor: String, first: Int): Future[Either[BetServiceError, BetHistory]]

  def saveBet(bet: Bet): Future[Either[BetServiceError, Unit]]

  def updateBet(bet: Bet): Future[Either[BetServiceError, Unit]]

}

class BetServiceImpl(betActor: ActorSelection) extends BetService {

  override def getBet(betId: String): Future[Either[BetServiceError, Bet]] = {
    val result = betActor ? BetActor.GetBet(betId)

    result.collect {
      case BetActor.GetBetResponse(Some(bet)) =>
        Right(bet)
      case BetActor.GetBetResponse(None) =>
        Left(BetServiceError(
          msg = s"Cannot find bet with ID '$betId'!",
          throwable = Some(BetNotFoundException)))
    }
  }

  override def getBetHistory(accountId: String, first: Int): Future[Either[BetServiceError, BetHistory]] = {
    val result = betActor ? BetActor.GetBetHistory(accountId, first)

    result.collect {
      case BetActor.GetBetHistoryResponse(betHistory) =>
        Right(betHistory)
    }
  }

  override def getBetHistoryFrom(accountId: String,
                                 cursor: String,
                                 first: Int): Future[Either[BetServiceError, BetHistory]] = {
    val result = betActor ? BetActor.GetBetHistoryFrom(accountId, cursor, first)

    result.collect {
      case BetActor.GetBetHistoryFromResponse(betHistory) =>
        Right(betHistory)
    }
  }

  override def getBetHistoryTo(accountId: String,
                               cursor: String,
                               first: Int): Future[Either[BetServiceError, BetHistory]] = {
    val result = betActor ? BetActor.GetBetHistoryTo(accountId, cursor, first)

    result.collect {
      case BetActor.GetBetHistoryToResponse(betHistory) =>
        Right(betHistory)
    }
  }

  override def saveBet(bet: Bet): Future[Either[BetServiceError, Unit]] = {
    val result = betActor ? BetActor.AddBet(bet)

    result.collect {
      case BetActor.BetAdded =>
        Right(())
      case BetActor.BetNotAdded =>
        Left(BetServiceError(
          msg = s"Bet with ID '${bet.betId}' already exists!",
          throwable = Some(BetAlreadyExistsException)))
    }
  }

  override def updateBet(bet: Bet): Future[Either[BetServiceError, Unit]] = {
    val result = betActor ? BetActor.UpdateBet(bet)

    result.collect {
      case BetActor.BetUpdated =>
        Right(())
      case BetActor.BetNotUpdated(OldBetUpdateData) =>
        Left(BetServiceError(
          msg = s"Bet with ID '${bet.betId}' not updated!",
          throwable = Some(OldBetException)))
      case BetActor.BetNotUpdated(BetDoesNotExist) =>
        Left(BetServiceError(
          msg = s"Bet with ID '${bet.betId}' does not exist!",
          throwable = Some(BetNotFoundException)))
    }
  }
}

object BetServiceImpl {

  def apply(actorSystem: ActorSystem): BetServiceImpl = {
    val betActor = actorSystem actorSelection "/user/bet-history-parent-actor/bet-actor"
    new BetServiceImpl(betActor)
  }

}

case object BetNotFoundException extends Exception

case object BetAlreadyExistsException extends Exception

case object OldBetException extends Exception
