package org.bet.history.service

import java.time.{LocalDateTime, ZoneOffset}

import org.bet.history.Utils.actorSystem
import org.bet.history.model.{Bet, BetHistory, BetStatus}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class BetServiceTest extends AnyFlatSpec with Matchers {

  private val bet1 = Bet.from(
    betId = "bet-001", accountId = "acc-001", outcomeId = "out-004", payout = "500.12", modified = "2020-09-03 15:12:16")

  private val bet2 = Bet.from(
    betId = "bet-002", accountId = "acc-001", outcomeId = "out-003", payout = "100", modified = "2020-09-07 15:21:11")

  private val bet3 = Bet.from(
    betId = "bet-003", accountId = "acc-001", outcomeId = "out-001", payout = "1.99", modified = "2020-09-12 16:11:15")

  private val bet4 = Bet.from(
    betId = "bet-004", accountId = "acc-001", outcomeId = "out-002", payout = "900.50", modified = "2020-09-17 17:51:01")

  private val betService = BetServiceImpl(actorSystem(Iterator(bet1, bet2, bet3, bet4)))

  "getBet" should "return bet" in {
    // given
    val accountId = "acc-001"
    val betId = "bet-001"

    // when
    val actual = Await.result(betService getBet betId, 3.seconds).toOption.get

    // then
    actual shouldBe bet1
  }

  it should "return service error for non-existing bet" in {
    // given
    val nonExistingBetId = "non-existing"

    // when
    val serviceError = Await.result(betService getBet nonExistingBetId, 3.seconds).swap.toOption.get
    val actual = serviceError.throwable.get

    // then
    actual shouldBe a[BetNotFoundException.type]
  }

  "getBetHistory" should "return bets" in {
    // given
    val accountId = "acc-001"
    val first = 2

    // when
    val actual = Await.result(betService.getBetHistory(accountId, first), 3.seconds).toOption.get

    // then
    actual shouldBe BetHistory(
      bets = List(bet4, bet3),
      hasNextPage = true,
      hasPreviousPage = false,
      firstCursor = Some(bet4.cursor),
      lastCursor = Some(bet1.cursor))
  }

  it should "return none bets" in {
    // given
    val nonExistingBetId = "non-existing"
    val first = 2

    // when
    val actual = Await.result(betService.getBetHistory(nonExistingBetId, first), 3.seconds).toOption.get

    // then
    actual shouldBe BetHistory(
      bets = List.empty,
      hasNextPage = false,
      hasPreviousPage = false,
      firstCursor = None,
      lastCursor = None)
  }

  "getBetHistoryFrom" should "return bets" in {
    // given
    val accountId = "acc-001"
    val cursor = bet3.cursor
    val first = 2

    // when
    val actual = Await.result(betService.getBetHistoryFrom(accountId, cursor, first), 3.seconds).toOption.get

    // then
    actual shouldBe BetHistory(
      bets = List(bet3, bet2),
      hasNextPage = true,
      hasPreviousPage = true,
      firstCursor = Some(bet4.cursor),
      lastCursor = Some(bet1.cursor))
  }

  it should "return none bets" in {
    // given
    val nonExistingBetId = "non-existing"
    val cursor = bet3.cursor
    val first = 2

    // when
    val actual = Await.result(betService.getBetHistoryFrom(nonExistingBetId, cursor, first), 3.seconds).toOption.get

    // then
    actual shouldBe BetHistory(
      bets = List.empty,
      hasNextPage = false,
      hasPreviousPage = false,
      firstCursor = None,
      lastCursor = None)
  }

  "getBetHistoryTo" should "return bets" in {
    // given
    val accountId = "acc-001"
    val cursor = bet2.cursor
    val first = 2

    // when
    val actual = Await.result(betService.getBetHistoryTo(accountId, cursor, first), 3.seconds).toOption.get

    // then
    actual shouldBe BetHistory(
      bets = List(bet3, bet2),
      hasNextPage = true,
      hasPreviousPage = true,
      firstCursor = Some(bet4.cursor),
      lastCursor = Some(bet1.cursor))
  }

  it should "return none bets" in {
    // given
    val nonExistingBetId = "non-existing"
    val cursor = bet2.cursor
    val first = 2

    // when
    val actual = Await.result(betService.getBetHistoryTo(nonExistingBetId, cursor, first), 3.seconds).toOption.get

    // then
    actual shouldBe BetHistory(
      bets = List.empty,
      hasNextPage = false,
      hasPreviousPage = false,
      firstCursor = None,
      lastCursor = None)
  }

  "saveBet" should "save a new bet" in {
    // given
    val betId = "bet-005"
    val bet = Bet.from(
      betId, accountId = "acc-001", outcomeId = "out-002", payout = "97.23", modified = "2020-09-19 17:51:01")

    // when
    Await.result(betService saveBet bet, 3.seconds)
    val actual = Await.result(betService getBet betId, 3.seconds).toOption.get

    // then
    actual shouldBe bet
  }

  it should "not save a bet" in {
    // given
    val betId = "bet-001"
    val bet = Bet.from(
      betId, accountId = "acc-001", outcomeId = "out-002", payout = "97.23", modified = "2020-09-19 17:51:01")

    // when
    val serviceError = Await.result(betService saveBet bet, 3.seconds).swap.toOption.get
    val actual = serviceError.throwable.get

    // then
    actual shouldBe a[BetAlreadyExistsException.type]
  }

  "updateBet" should "update an existing bet" in {
    // given
    val newStatus = BetStatus.Won
    val timestamp = LocalDateTime.of(2020, 9, 30, 12, 1).toInstant(ZoneOffset.UTC)

    val bet = bet1.copy(status = newStatus, modified = timestamp)

    // when
    Await.result(betService updateBet bet, 3.seconds)
    val actual = Await.result(betService getBet bet.betId, 3.seconds).toOption.get

    // then
    actual.betId shouldBe bet.betId
    actual.status shouldBe newStatus
    actual.modified shouldBe timestamp
  }

  it should "not update non-existing bet" in {
    // given
    val nonExistingBetId = "non-existing"
    val bet = bet1.copy(betId = nonExistingBetId)

    // when
    val serviceError = Await.result(betService updateBet bet, 3.seconds).swap.toOption.get
    val actual = serviceError.throwable.get

    // then
    actual shouldBe a[BetNotFoundException.type]
  }

  it should "not update an existing bet" in {
    // given
    val newStatus = BetStatus.Won
    val timestamp = LocalDateTime.of(2020, 9, 1, 12, 1).toInstant(ZoneOffset.UTC)

    val bet = bet1.copy(status = newStatus, modified = timestamp)

    // when
    val serviceError = Await.result(betService updateBet bet, 3.seconds).swap.toOption.get
    val actual = serviceError.throwable.get

    // then
    actual shouldBe a[OldBetException.type]
  }

}
