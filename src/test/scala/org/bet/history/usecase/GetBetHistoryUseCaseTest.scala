package org.bet.history.usecase

import org.bet.history.dto.{BetHistoryDTO, ErrorDTO, NavigationDTO, NodeDTO}
import org.bet.history.model._
import org.bet.history.service.{BetService, OutcomeService}
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class GetBetHistoryUseCaseTest extends AnyFlatSpec with Matchers with MockitoSugar {

  private val accountId = "acc-001"
  private val betId = "bet-001"
  private val outcomeId = "out-001"
  private val payout = "500.12"
  private val fixtureName = "Fixture Name"
  private val outcomeName = "Outcome Name"
  private val hasNextPage = true
  private val hasPreviousPage = true
  private val firstCursor = "123"
  private val lastCursor = "321"

  private val bet = Bet.from(betId, accountId, outcomeId, payout, modified = "2020-09-03 15:12:16")
  private val betHistory = BetHistory(bets = List(bet), hasNextPage, hasPreviousPage, Some(firstCursor), Some(lastCursor))
  private val account = Account(id = accountId, name = "Account Name", phoneNumber = "555-0152")
  private val outcome = Outcome(id = outcomeId, fixtureName, outcomeName)

  "betHistory" should "return bet history" in {
    // given
    val first = "10"

    val betService = mock[BetService]
    val outcomeService = mock[OutcomeService]

    when(betService.getBetHistory(accountId, first.toInt)).thenReturn(Future successful Right(betHistory))
    when(outcomeService getOutcome outcomeId).thenReturn(Future successful Right(outcome))

    val getBetHistoryUseCase = GetBetHistoryUseCase(betService, outcomeService)

    // when
    val actual = Await.result(getBetHistoryUseCase.betHistory(accountId, first, after = None, before = None), 3.seconds)

    // then
    actual shouldBe BetHistoryDTO(
      nodes = List(NodeDTO(
        betId, payout.toDouble, status = BetStatus.Open.toString, fixtureName, outcomeName, cursor = bet.cursor)),
      navigation = NavigationDTO(hasNextPage, hasPreviousPage, firstCursor, lastCursor))

    verify(betService).getBetHistory(accountId, first.toInt)
    verify(outcomeService).getOutcome(outcomeId)
  }

  it should "return bet history with 'after' argument specified" in {
    // given
    val first = "10"
    val after = bet.cursor

    val betService = mock[BetService]
    val outcomeService = mock[OutcomeService]

    when(betService.getBetHistoryFrom(accountId, after, first.toInt)).thenReturn(Future successful Right(betHistory))
    when(outcomeService getOutcome outcomeId).thenReturn(Future successful Right(outcome))

    val getBetHistoryUseCase = GetBetHistoryUseCase(betService, outcomeService)

    // when
    val actual = Await.result(getBetHistoryUseCase.betHistory(accountId, first, Some(after), before = None), 3.seconds)

    // then
    actual shouldBe BetHistoryDTO(
      nodes = List(NodeDTO(
        betId, payout.toDouble, status = BetStatus.Open.toString, fixtureName, outcomeName, cursor = bet.cursor)),
      navigation = NavigationDTO(hasNextPage, hasPreviousPage, firstCursor, lastCursor))

    verify(betService).getBetHistoryFrom(accountId, after, first.toInt)
    verify(outcomeService).getOutcome(outcomeId)
  }

  it should "return bet history with 'before' argument specified" in {
    // given
    val first = "10"
    val before = bet.cursor

    val betService = mock[BetService]
    val outcomeService = mock[OutcomeService]

    when(betService.getBetHistoryTo(accountId, before, first.toInt)).thenReturn(Future successful Right(betHistory))
    when(outcomeService getOutcome outcomeId).thenReturn(Future successful Right(outcome))

    val getBetHistoryUseCase = GetBetHistoryUseCase(betService, outcomeService)

    // when
    val actual = Await.result(getBetHistoryUseCase.betHistory(accountId, first, after = None, Some(before)), 3.seconds)

    // then
    actual shouldBe BetHistoryDTO(
      nodes = List(NodeDTO(
        betId, payout.toDouble, status = BetStatus.Open.toString, fixtureName, outcomeName, cursor = bet.cursor)),
      navigation = NavigationDTO(hasNextPage, hasPreviousPage, firstCursor, lastCursor))

    verify(betService).getBetHistoryTo(accountId, before, first.toInt)
    verify(outcomeService).getOutcome(outcomeId)
  }

  it should "return error response if argument 'first' is not positive integer" in {
    // given
    val first = "-1"

    val betService = mock[BetService]
    val outcomeService = mock[OutcomeService]

    val getBetHistoryUseCase = GetBetHistoryUseCase(betService, outcomeService)

    // when
    val actual = Await.result(getBetHistoryUseCase.betHistory(accountId, first, after = None, before = None), 3.seconds)

    // then
    actual shouldBe ErrorDTO(errorMsg = "Argument 'first' must be positive integer!")
  }

  it should "return error response if both arguments 'after' and 'before' are specified" in {
    // given
    val first = "10"
    val after = bet.cursor
    val before = bet.cursor

    val betService = mock[BetService]
    val outcomeService = mock[OutcomeService]

    val getBetHistoryUseCase = GetBetHistoryUseCase(betService, outcomeService)

    // when
    val actual = Await.result(getBetHistoryUseCase.betHistory(accountId, first, Some(after), Some(before)), 3.seconds)

    // then
    actual shouldBe ErrorDTO(errorMsg = "Both arguments 'after' and 'before' cannot be specified!")
  }

}
