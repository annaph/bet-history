package org.bet.history.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.bet.history.event._
import org.bet.history.streaming.EventSourcesImpl.sleep

import scala.concurrent.blocking
import scala.io.{Source => IOSource}
import scala.util.Random

trait EventSources {

  def betPlacementEvents: Source[BetPlacementEvent, NotUsed]

  def betSettlementEvents: Source[BetSettlementEvent, NotUsed]

}

class EventSourcesImpl(betPlacements: List[BetPlacementEvent],
                       betSettlements: List[BetSettlementEvent],
                       delayEmissions: Boolean) extends EventSources {

  override def betPlacementEvents: Source[BetPlacementEvent, NotUsed] =
    Source.fromIterator(() => betPlacements.iterator).map(delay)

  override def betSettlementEvents: Source[BetSettlementEvent, NotUsed] =
    Source.fromIterator(() => betSettlements.iterator).map(delay)

  private def delay[T](event: T): T = {
    if (delayEmissions) sleep()
    event
  }

}

object EventSourcesImpl {

  private val random = new Random()

  def apply(betPlacementsInputFile: String = "bet-placement-events.csv",
            betSettlementsInputFile: String = "bet-settlement-events.csv",
            delayEmissions: Boolean = true): EventSources = {
    val betPlacements = readBetPlacementEvents(betPlacementsInputFile)
    val betSettlements = readBetSettlementEvents(betSettlementsInputFile)

    new EventSourcesImpl(betPlacements, betSettlements, delayEmissions)
  }

  private def readBetPlacementEvents(inputFilePath: String): List[BetPlacementEvent] =
    readEvents(inputFilePath)(parseBetPlacementLine)

  private def readBetSettlementEvents(inputFilePath: String): List[BetSettlementEvent] =
    readEvents(inputFilePath)(parseBetSettlementLine)

  private def readEvents[T](inputFilePath: String)(readLine: String => T): List[T] = {
    val source = IOSource fromResource inputFilePath

    val events = source.getLines().drop(1).map(readLine).toList
    source.close()

    Random shuffle events
  }

  private def parseBetPlacementLine(line: String): BetPlacementEvent = {
    line.split(",").map(_.trim) match {
      case Array(betId, accountId, outcomeId, payout, timestamp) =>
        BetPlaced.from(betId, accountId, outcomeId, payout, timestamp)
      case _ =>
        throw new Exception("Error parsing bet placements input file!")
    }
  }

  private def parseBetSettlementLine(line: String): BetSettlementEvent = {
    line.split(",").map(_.trim) match {
      case Array(betId, "won", timestamp) =>
        BetWon.from(betId, timestamp)
      case Array(betId, "lost", timestamp) =>
        BetLost.from(betId, timestamp)
      case _ =>
        throw new Exception("Error parsing bet settlement input file!")
    }
  }

  private def sleep(): Unit = {
    val millis = random.between(1, 2001)

    blocking {
      Thread sleep millis
    }
  }

}
