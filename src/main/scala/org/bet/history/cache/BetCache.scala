package org.bet.history.cache

import java.time.{Instant, LocalDateTime, ZoneOffset}

import org.bet.history.cache.BetCacheImpl.{BetsAscOrdering, BetsDescOrdering}
import org.bet.history.model.Bet

import scala.collection.mutable

trait BetCache {

  def bet(betId: String): Option[Bet]

  def bets(accountId: String, cursor: Option[String] = None, count: Int = 0): List[Bet]

  def firstCursor(accountId: String): Option[String]

  def lastCursor(accountId: String): Option[String]

  def add(bet: Bet): Option[Bet]

  def update(bet: Bet): Option[Bet]

}

class BetCacheImpl() extends BetCache {

  private[cache] val betIdToBet = mutable.Map.empty[String, Bet]

  private[cache] val cursorToBet = mutable.Map.empty[String, Bet]

  private[cache] val accountIdToBetsDesc = mutable.Map.empty[String, mutable.SortedSet[Bet]]

  private[cache] val accountIdToBetsAsc = mutable.Map.empty[String, mutable.SortedSet[Bet]]

  override def bet(betId: String): Option[Bet] =
    betIdToBet.get(betId)

  override def bets(accountId: String, cursor: Option[String] = None, count: Int = 0): List[Bet] =
    (cursor, count) match {
      case (_, 0) =>
        List.empty

      case (Some(c), _) if !cursorToBet.contains(c) =>
        List.empty

      case (_, n) if n > 0 =>
        accountIdToBetsDesc.get(accountId)
          .map(bets => cursorToBet(cursor getOrElse bets.head.cursor))
          .map(bet => accountIdToBetsDesc(accountId).rangeFrom(bet))
          .map(_.take(count))
          .map(_.toList)
          .getOrElse(List.empty)

      case _ =>
        accountIdToBetsAsc.get(accountId)
          .map(bets => cursorToBet(cursor getOrElse bets.head.cursor))
          .map(bet => accountIdToBetsAsc(accountId).rangeFrom(bet))
          .map(_.take(Math abs count))
          .map(_.toList.reverse)
          .getOrElse(List.empty)
    }

  override def firstCursor(accountId: String): Option[String] =
    accountIdToBetsDesc.get(accountId).map(_.head).map(_.cursor)

  override def lastCursor(accountId: String): Option[String] =
    accountIdToBetsAsc.get(accountId).map(_.head).map(_.cursor)

  override def add(bet: Bet): Option[Bet] = betIdToBet.get(bet.betId) match {
    case Some(_) =>
      None
    case None =>
      addBet(bet)
      Some(bet)
  }

  override def update(bet: Bet): Option[Bet] =
    betIdToBet.get(bet.betId)
      .map(existingBet => updateBet(existingBet, bet))
      .map(_ => bet)

  private def updateBet(oldBet: Bet, newBet: Bet): Unit = {
    accountIdToBetsDesc(oldBet.accountId).remove(oldBet)
    accountIdToBetsAsc(oldBet.accountId).remove(oldBet)

    betIdToBet.remove(oldBet.betId)
    cursorToBet.remove(oldBet.cursor)

    addBet(newBet)
  }

  private def addBet(bet: Bet): Unit = {
    betIdToBet += (bet.betId -> bet)
    cursorToBet += (bet.cursor -> bet)

    accountIdToBetsDesc.get(bet.accountId) match {
      case Some(bets) =>
        bets += bet
      case None =>
        val bets = mutable.SortedSet[Bet](bet)(BetsDescOrdering)
        accountIdToBetsDesc += (bet.accountId -> bets)
    }

    accountIdToBetsAsc.get(bet.accountId) match {
      case Some(bets) =>
        bets += bet
      case None =>
        val bets = mutable.SortedSet[Bet](bet)(BetsAscOrdering)
        accountIdToBetsAsc += (bet.accountId -> bets)
    }
  }

  override def toString: String =
    accountIdToBetsDesc.toString()

}

object BetCacheImpl {

  def apply(initBets: Bet*): BetCacheImpl = {
    val betCache = new BetCacheImpl
    initBets.foreach(betCache.add)

    betCache
  }

  object BetsDescOrdering extends Ordering[Bet] {

    override def compare(bet1: Bet, bet2: Bet): Int =
      if (bet1 equals bet2) 0 else {
        val dt1 = bet1.modified.toLocalDateTime
        val dt2 = bet2.modified.toLocalDateTime

        if (dt1 isAfter dt2) -1 else 1
      }

  }

  object BetsAscOrdering extends Ordering[Bet] {

    override def compare(bet1: Bet, bet2: Bet): Int =
      if (bet1 equals bet2) 0 else {
        val dt1 = bet1.modified.toLocalDateTime
        val dt2 = bet2.modified.toLocalDateTime

        if (dt1 isBefore dt2) -1 else 1
      }

  }

  implicit class InstantOps(instant: Instant) {

    def toLocalDateTime: LocalDateTime =
      LocalDateTime.ofInstant(instant, ZoneOffset.UTC)

  }

}
