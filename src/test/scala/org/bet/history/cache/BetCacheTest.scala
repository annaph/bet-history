package org.bet.history.cache

import org.bet.history.model.Bet
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class BetCacheTest extends AnyFlatSpec with Matchers {

  "add" should "add new bets into cache" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")

    val betCache = BetCacheImpl()

    // when
    val actual1 = betCache.add(bet1).get
    val actual2 = betCache.add(bet2).get
    val actual3 = betCache.add(bet1)

    // then
    betCache.betIdToBet should equal(mutable.Map(betId1 -> bet1, betId2 -> bet2))
    betCache.cursorToBet should equal(mutable.Map(bet1.cursor -> bet1, bet2.cursor -> bet2))

    betCache.accountIdToBetsDesc.size shouldBe 1
    betCache.accountIdToBetsDesc(accountId).toList should equal(List(bet2, bet1))

    betCache.accountIdToBetsAsc.size shouldBe 1
    betCache.accountIdToBetsAsc(accountId).toList should equal(List(bet1, bet2))

    actual1 shouldBe bet1
    actual2 shouldBe bet2
    actual3 shouldBe None
  }

  "get" should "retrieve bet from cache" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"

    val bet = Bet.from(
      betId = betId1, accountId = "acc-001", outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")

    val betCache = BetCacheImpl(bet)

    // when
    val actual1 = betCache.bet(betId1).get
    val actual2 = betCache.bet(betId2)

    // then
    actual1 shouldBe bet
    actual2 shouldBe None
  }

  "update" should "update bet in the cache" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val betId3 = "bet-003"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet1Modified = Bet.from(
      betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-19 15:12:15")

    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")
    val bet3 = Bet.from(betId3, accountId, outcomeId = "out-002", payout = "3.3", modified = "2020-09-23 15:12:15")

    val betCache = BetCacheImpl(bet1, bet2)

    // when
    val actual1 = betCache.update(bet1Modified).get
    val actual2 = betCache.update(bet3)

    // then
    betCache.betIdToBet should equal(mutable.Map(betId1 -> bet1Modified, betId2 -> bet2))
    betCache.cursorToBet should equal(mutable.Map(bet1.cursor -> bet1Modified, bet2.cursor -> bet2))

    betCache.accountIdToBetsDesc.size shouldBe 1
    betCache.accountIdToBetsDesc(accountId).toList should equal(List(bet1Modified, bet2))

    betCache.accountIdToBetsAsc.size shouldBe 1
    betCache.accountIdToBetsAsc(accountId).toList should equal(List(bet2, bet1Modified))

    actual1 shouldBe bet1Modified
    actual2 shouldBe None
  }

  "bets" should "return bets from latest to oldest" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val betId3 = "bet-003"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")
    val bet3 = Bet.from(betId3, accountId, outcomeId = "out-002", payout = "3.3", modified = "2020-09-23 15:12:15")

    val betCache = BetCacheImpl(bet2, bet3, bet1)

    // when
    val actual = betCache.bets(accountId, count = 2)

    // then
    actual shouldBe List(bet3, bet2)
  }

  it should "return bets from oldest to latest reversed" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val betId3 = "bet-003"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")
    val bet3 = Bet.from(betId3, accountId, outcomeId = "out-002", payout = "3.3", modified = "2020-09-23 15:12:15")

    val betCache = BetCacheImpl(bet2, bet3, bet1)

    // when
    val actual = betCache.bets(accountId, count = -2)

    // then
    actual shouldBe List(bet2, bet1)
  }

  it should "return bets from latest to oldest with cursor specified" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val betId3 = "bet-003"
    val betId4 = "bet-004"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")
    val bet3 = Bet.from(betId3, accountId, outcomeId = "out-002", payout = "3.3", modified = "2020-09-19 15:12:15")
    val bet4 = Bet.from(betId4, accountId, outcomeId = "out-002", payout = "4.4", modified = "2020-09-23 15:12:15")

    val betCache = BetCacheImpl(bet2, bet4, bet3, bet1)

    // when
    val actual = betCache.bets(accountId, cursor = Some(bet3.cursor), count = 2)

    // then
    actual shouldBe List(bet3, bet2)
  }

  it should "return bets from oldest to latest reversed with cursor specified" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val betId3 = "bet-003"
    val betId4 = "bet-004"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")
    val bet3 = Bet.from(betId3, accountId, outcomeId = "out-002", payout = "3.3", modified = "2020-09-19 15:12:15")
    val bet4 = Bet.from(betId4, accountId, outcomeId = "out-002", payout = "4.4", modified = "2020-09-23 15:12:15")

    val betCache = BetCacheImpl(bet2, bet4, bet3, bet1)

    // when
    val actual = betCache.bets(accountId, cursor = Some(bet2.cursor), count = -2)

    // then
    actual shouldBe List(bet3, bet2)
  }

  it should "return none bet for non existing account ID" in {
    // given
    val accountId = "none-existing"
    val betCache = BetCacheImpl()

    // when
    val actual = betCache.bets(accountId)

    // then
    actual.length shouldBe 0
  }

  it should "return none bet for count = 0" in {
    // given
    val accountId = "acc-001"
    val bet = Bet.from(
      betId = "bet-001", accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")

    val betCache = BetCacheImpl(bet)

    // when
    val actual = betCache.bets(accountId)

    // then
    actual.length shouldBe 0
  }

  "firstCursor" should "return first cursor" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")

    val betCache = BetCacheImpl(bet1, bet2)

    // when
    val actual = betCache.firstCursor(accountId).get

    // then
    actual shouldBe bet2.cursor
  }

  it should "return none cursor" in {
    // given
    val accountId = "acc-001"
    val betCache = BetCacheImpl()

    // when
    val actual = betCache.firstCursor(accountId)

    // then
    actual shouldBe None
  }

  "lastCursor" should "return last cursor" in {
    // given
    val betId1 = "bet-001"
    val betId2 = "bet-002"
    val accountId = "acc-001"

    val bet1 = Bet.from(betId1, accountId, outcomeId = "out-001", payout = "1.1", modified = "2020-09-07 15:12:15")
    val bet2 = Bet.from(betId2, accountId, outcomeId = "out-002", payout = "2.2", modified = "2020-09-12 15:12:15")

    val betCache = BetCacheImpl(bet1, bet2)

    // when
    val actual = betCache.lastCursor(accountId).get

    // then
    actual shouldBe bet1.cursor
  }

  it should "return none cursor" in {
    // given
    val accountId = "acc-001"
    val betCache = BetCacheImpl()

    // when
    val actual = betCache.lastCursor(accountId)

    // then
    actual shouldBe None
  }

}
