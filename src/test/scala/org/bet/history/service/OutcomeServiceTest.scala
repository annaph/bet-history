package org.bet.history.service

import org.bet.history.model.Outcome
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class OutcomeServiceTest extends AnyFlatSpec with Matchers {

  private val outcomeService = OutcomeServiceImpl(inputFilePath = "test-outcomes.csv")

  "getOutcome" should "return Outcome" in {
    // given
    val outcomeId = "out-0001"

    // when
    val actual = Await.result(outcomeService getOutcome outcomeId, 3.seconds).toOption.get

    // then
    actual shouldBe Outcome(
      id = outcomeId,
      fixtureName = "Real Madrid vs Barcelona",
      outcomeName = "Barcelona wins match")
  }

  it should "return service error for non-existing outcome" in {
    // given
    val nonExistingOutcomeId = "non-existing"

    // when
    val serviceError = Await.result(outcomeService getOutcome nonExistingOutcomeId, 3.seconds).swap.toOption.get
    val actual = serviceError.throwable.get

    // then
    actual shouldBe a[OutcomeNotFoundException.type]
  }

}
