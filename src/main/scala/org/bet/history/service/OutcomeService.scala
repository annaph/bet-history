package org.bet.history.service

import org.bet.history.model.{Outcome, OutcomeServiceError}

import scala.concurrent.Future

trait OutcomeService {

  def getOutcome(outcomeId: String): Future[Either[OutcomeServiceError, Outcome]]

}

class OutcomeServiceImpl(outcomes: List[Outcome], keySelector: Outcome => String)
  extends RepositoryService(outcomes, keySelector) with OutcomeService {

  override def getOutcome(outcomeId: String): Future[Either[OutcomeServiceError, Outcome]] = {
    lazy val serviceError = OutcomeServiceError(
      msg = s"No outcome with ID '$outcomeId'",
      throwable = Some(OutcomeNotFoundException))
    getItem(outcomeId, serviceError)
  }

}

object OutcomeServiceImpl {

  def apply(inputFilePath: String = "outcomes.csv"): OutcomeServiceImpl =
    new OutcomeServiceImpl(
      outcomes = RepositoryService.readResource(inputFilePath)(parseLine),
      keySelector = outcome => outcome.id)

  private def parseLine(line: String): Outcome =
    line.split(",").map(_.trim) match {
      case Array(id, fixtureName, outcomeName) =>
        Outcome(id, fixtureName, outcomeName)
      case _ =>
        throw new Exception("Error parsing outcomes input file!")
    }

}

case object OutcomeNotFoundException extends Exception
