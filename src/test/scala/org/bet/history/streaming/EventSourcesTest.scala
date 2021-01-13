package org.bet.history.streaming

import org.bet.history.Utils.materializer
import org.bet.history.event._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class EventSourcesTest extends AnyFlatSpec with Matchers {

  private val eventSources = EventSourcesImpl(
    betPlacementsInputFile = "test-bet-placement-events.csv",
    betSettlementsInputFile = "test-bet-settlement-events.csv",
    delayEmissions = false)

  "betPlacementEvents" should "stream bet placement events" in {
    // given
    val betPlaced1 = BetPlaced.from(
      betId = "bet-0001", accountId = "acc-0002", outcomeId = "out-0002",
      payout = "10.89", timestamp = "2020-09-05 15:12:15")

    val betPlaced2 = BetPlaced.from(
      betId = "bet-0002", accountId = "acc-0001", outcomeId = "out-0004",
      payout = "500.12", timestamp = "2020-09-05 15:12:16")

    val betPlaced3 = BetPlaced.from(
      betId = "bet-0003", accountId = "acc-0002", outcomeId = "out-0002",
      payout = "1.05", timestamp = "2020-09-05 15:19:30")

    // when
    val actual = mutable.Set.empty[BetPlacementEvent]
    val eventSource = eventSources.betPlacementEvents.runForeach(actual += _)

    Await.result(eventSource, 3.seconds)

    // then
    actual shouldBe Set(betPlaced1, betPlaced2, betPlaced3)
  }

  "betSettlementEvents" should "stream bet settlement events" in {
    // given
    val betWon1 = BetWon.from(betId = "bet-0001", timestamp = "2020-09-06 10:45:15")
    val betWon2 = BetWon.from(betId = "bet-0002", timestamp = "2020-09-07 12:00:00")
    val betLost = BetLost.from(betId = "bet-0003", timestamp = "2020-09-06 09:10:00")

    // when
    val actual = mutable.Set.empty[BetSettlementEvent]
    val eventSource = eventSources.betSettlementEvents.runForeach(actual += _)

    Await.result(eventSource, 3.seconds)

    // then
    actual shouldBe Set(betWon1, betWon2, betLost)
  }

}
