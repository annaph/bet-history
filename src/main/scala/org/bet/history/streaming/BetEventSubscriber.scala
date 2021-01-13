package org.bet.history.streaming

import java.time.Instant

import akka.Done
import akka.stream.Materializer
import org.bet.history.event._
import org.bet.history.model._
import org.bet.history.service.{AccountService, BetService, NotificationService, OutcomeService}
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BetEventSubscriber(eventSources: EventSources,
                         betService: BetService,
                         outcomeService: OutcomeService,
                         accountService: AccountService,
                         notificationService: NotificationService)
                        (implicit val materializer: Materializer) extends ApplicationListener[ContextRefreshedEvent] {

  override def onApplicationEvent(appEvent: ContextRefreshedEvent): Unit =
    for {
      _ <- betPlacementStream
      _ <- betSettlementStream
    } println("Finished streaming bet settlement events.")

  private def betPlacementStream: Future[Done] =
    eventSources.betPlacementEvents
      .map(toBet)
      .filter(_.nonEmpty)
      .map(_.get)
      .mapAsync(parallelism = 1)(saveBet)
      .run()

  private def toBet(betPlacementEvent: BetPlacementEvent): Option[Bet] = betPlacementEvent match {
    case BetPlaced(betId, accountId, outcomeId, payout, timestamp) =>
      val bet = Bet(betId, accountId, outcomeId, payout, status = BetStatus.Open, timestamp)
      Some(bet)
    case _ =>
      None
  }

  private def saveBet(bet: Bet): Future[Unit] =
    betService.saveBet(bet).map(_ => ())

  private def betSettlementStream: Future[Done] =
    eventSources.betSettlementEvents
      .mapAsync(parallelism = 1)(updateBet)
      .filter(_.nonEmpty)
      .map(_.get)
      .filter(_.status == BetStatus.Won)
      .mapAsync(parallelism = 7)(sendNotification)
      .run()

  private def updateBet(betSettlementEvent: BetSettlementEvent): Future[Option[Bet]] = {
    val (betId, timestamp, status) = betSettlementEvent match {
      case BetWon(betId, timestamp) =>
        (betId, timestamp, BetStatus.Won)
      case BetLost(betId, timestamp) =>
        (betId, timestamp, BetStatus.Lost)
    }

    betService.getBet(betId).flatMap {
      case Right(bet) =>
        updateBetStatusAndTimestamp(bet, status, timestamp)
      case _ =>
        Future(None)
    }
  }

  private def updateBetStatusAndTimestamp(bet: Bet, newStatus: BetStatus, timestamp: Instant): Future[Option[Bet]] = {
    val newBet = bet.copy(status = newStatus, modified = timestamp)

    betService.updateBet(newBet)
      .map(_.toOption.map(_ => newBet))
  }

  private def sendNotification(bet: Bet): Future[Unit] =
    for {
      outcome <- getOutcome(bet.outcomeId)
      account <- getAccount(bet.accountId)
    } yield {
      (outcome, account) match {
        case (Some(Outcome(_, fixtureName, outcomeName)), Some(Account(_, name, phoneNumber))) =>
          val msg =
            s"""
               |$name, congratulations you just won ${bet.payout} on $fixtureName, $outcomeName
               | betId: '${bet.betId}'
               | timestamp: '${bet.modified}'
               |""".stripMargin

          notificationService send Notification(phoneNumber, msg)

        case _ =>
          ()
      }
    }

  private def getOutcome(outcomeId: String): Future[Option[Outcome]] =
    outcomeService.getOutcome(outcomeId).map(_.toOption)

  private def getAccount(accountId: String): Future[Option[Account]] =
    accountService.getAccount(accountId).map(_.toOption)

}

object BetEventSubscriber {

  def apply(eventSources: EventSources,
            betService: BetService,
            outcomeService: OutcomeService,
            accountService: AccountService,
            notificationService: NotificationService,
            materializer: Materializer): BetEventSubscriber =
    new BetEventSubscriber(eventSources, betService, outcomeService, accountService, notificationService)(materializer)

}
