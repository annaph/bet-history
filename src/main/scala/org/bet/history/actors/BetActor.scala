package org.bet.history.actors

import akka.actor.{Actor, Props}
import org.bet.history.actors.BetActor._
import org.bet.history.cache.BetCacheImpl
import org.bet.history.cache.BetCacheImpl.InstantOps
import org.bet.history.model.{Bet, BetHistory}

class BetActor(initBets: Iterator[Bet]) extends Actor {

  private val betCache = BetCacheImpl(initBets.toSeq: _*)

  override def receive: Receive = {
    case GetBet(betId) =>
      fetchBet(betId) match {
        case Some(bet) =>
          sender() ! GetBetResponse(Some(bet))
        case None =>
          sender() ! GetBetResponse(None)
      }

    case GetBetHistory(accountId, first) =>
      val betHistory = fetchBetHistory(accountId, None, first)
      sender() ! GetBetHistoryResponse(betHistory)

    case GetBetHistoryFrom(accountId, cursor, first) =>
      val betHistory = fetchBetHistory(accountId, Some(cursor), first)
      sender() ! GetBetHistoryFromResponse(betHistory)

    case GetBetHistoryTo(accountId, cursor, first) =>
      val betHistory = fetchBetHistory(accountId, Some(cursor), -first)
      sender() ! GetBetHistoryToResponse(betHistory)

    case AddBet(bet) =>
      addBet(bet) match {
        case Some(_) =>
          sender() ! BetAdded
        case None =>
          sender() ! BetNotAdded
      }

    case UpdateBet(bet) =>
      fetchBet(bet.betId) match {
        case Some(existingBet) if bet.modified.toLocalDateTime isAfter existingBet.modified.toLocalDateTime =>
          updateBet(bet)
          sender() ! BetUpdated
        case Some(_) =>
          sender() ! BetNotUpdated(reason = OldBetUpdateData)
        case None =>
          sender() ! BetNotUpdated(reason = BetDoesNotExist)
      }
  }

  private def fetchBet(betId: String): Option[Bet] =
    betCache bet betId

  private def fetchBetHistory(accountId: String, cursor: Option[String], first: Int): BetHistory = {
    val bets = betCache.bets(accountId, cursor, count = first)

    bets match {
      case firstBet :: otherBets =>
        val firstCursorInBets = firstBet.cursor
        val lastCursorInBets = if (otherBets.isEmpty) firstBet.cursor else otherBets.last.cursor

        val hasNextPage = betCache.bets(accountId, cursor = Some(lastCursorInBets), count = 2).length > 1
        val hasPreviousPage = betCache.bets(accountId, cursor = Some(firstCursorInBets), count = -2).length > 1

        val firstCursor = betCache firstCursor accountId
        val lastCursor = betCache lastCursor accountId

        BetHistory(bets, hasNextPage, hasPreviousPage, firstCursor, lastCursor)

      case Nil =>
        BetHistory(
          bets = List.empty, hasNextPage = false, hasPreviousPage = false, firstCursor = None, lastCursor = None)
    }
  }

  private def addBet(bet: Bet): Option[Bet] =
    betCache add bet

  private def updateBet(bet: Bet): Unit =
    betCache update bet

}

object BetActor {

  def props(initBets: Iterator[Bet]): Props =
    Props(new BetActor(initBets))

  sealed trait BetNotUpdatedReason

  case class GetBet(betId: String)

  case class GetBetResponse(bet: Option[Bet])

  case class GetBetHistory(accountId: String, first: Int)

  case class GetBetHistoryResponse(betHistory: BetHistory)

  case class GetBetHistoryFrom(accountId: String, cursor: String, first: Int)

  case class GetBetHistoryFromResponse(betHistory: BetHistory)

  case class GetBetHistoryTo(accountId: String, cursor: String, first: Int)

  case class GetBetHistoryToResponse(betHistory: BetHistory)

  case class AddBet(bet: Bet)

  case class UpdateBet(bet: Bet)

  case class BetNotUpdated(reason: BetNotUpdatedReason)

  case object BetAdded

  case object BetNotAdded

  case object BetUpdated

  case object BetDoesNotExist extends BetNotUpdatedReason

  case object OldBetUpdateData extends BetNotUpdatedReason

}
