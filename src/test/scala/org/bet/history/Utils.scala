package org.bet.history

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.bet.history.actors.BetHistoryParentActor
import org.bet.history.model.Bet

object Utils {

  def actorSystem(initBets: Iterator[Bet] = Iterator.empty): ActorSystem = {
    val actorSystem = ActorSystem("BetHistoryTestActorSystem")
    actorSystem.actorOf(BetHistoryParentActor.props(initBets), name = "bet-history-parent-actor")

    actorSystem
  }

  implicit val materializer: Materializer = Materializer(actorSystem())

}
