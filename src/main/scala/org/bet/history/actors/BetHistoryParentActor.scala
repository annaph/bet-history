package org.bet.history.actors

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import org.bet.history.model.Bet

class BetHistoryParentActor(initBets: Iterator[Bet]) extends Actor {

  context.actorOf(BetActor.props(initBets), name = "bet-actor")
  context.actorOf(NotificationActor.props, name = "notification-actor")

  override def receive: Receive = PartialFunction.empty

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e =>
      e.printStackTrace()
      Resume
  }
}

object BetHistoryParentActor {

  def props(initBets: Iterator[Bet] = Iterator.empty): Props =
    Props(new BetHistoryParentActor(initBets))

}
