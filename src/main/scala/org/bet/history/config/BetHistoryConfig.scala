package org.bet.history.config

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.bet.history.actors.BetHistoryParentActor
import org.bet.history.controller.BetHistoryController
import org.bet.history.service.{AccountService, AccountServiceImpl, BetService, BetServiceImpl, NotificationService, NotificationServiceImpl, OutcomeService, OutcomeServiceImpl}
import org.bet.history.streaming.{BetEventSubscriber, EventSources, EventSourcesImpl}
import org.bet.history.usecase.GetBetHistoryUseCase
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class BetHistoryConfig {

  @Bean
  @Qualifier(value = "bet-history-controller")
  def betHistoryController(getBetHistoryUseCase: GetBetHistoryUseCase,
                           objectMapper: ObjectMapper): BetHistoryController =
    BetHistoryController(getBetHistoryUseCase, objectMapper)

  @Bean
  @Qualifier(value = "bet-event-subscriber")
  def betEventSubscriber(eventSources: EventSources,
                         betService: BetService,
                         outcomeService: OutcomeService,
                         accountService: AccountService,
                         notificationService: NotificationService,
                         actorMaterializer: Materializer): BetEventSubscriber =
    BetEventSubscriber(eventSources, betService, outcomeService, accountService, notificationService, actorMaterializer)

  @Bean
  @Qualifier(value = "get-bet-history-use-case")
  def getBetHistoryUseCase(betService: BetService, outcomeService: OutcomeService): GetBetHistoryUseCase =
    GetBetHistoryUseCase(betService, outcomeService)

  @Bean
  @Qualifier(value = "outcome-service")
  def outcomeService(): OutcomeService =
    OutcomeServiceImpl()

  @Bean
  @Qualifier(value = "account-service")
  def accountService(): AccountService =
    AccountServiceImpl()

  @Bean
  @Qualifier(value = "notification-service")
  def notificationService(actorSystem: ActorSystem): NotificationService =
    NotificationServiceImpl(actorSystem)

  @Bean
  @Qualifier(value = "bet-service")
  def betService(actorSystem: ActorSystem): BetService =
    BetServiceImpl(actorSystem)

  @Bean
  @Qualifier(value = "event-sources")
  def eventSources(): EventSources =
    EventSourcesImpl()

  @Bean
  @Qualifier(value = "bet-history-actor-system")
  def actorSystem(): ActorSystem = {
    val actorSystem = ActorSystem("BetHistoryActorSystem")
    actorSystem.actorOf(BetHistoryParentActor.props(), name = "bet-history-parent-actor")

    actorSystem
  }

  @Bean
  @Qualifier(value = "actor-materializer")
  def actorMaterializer(actorSystem: ActorSystem): Materializer =
    Materializer(actorSystem)

  @Bean
  @Qualifier(value = "object-mapper")
  def objectMapper(): ObjectMapper =
    JsonMapper.builder()
      .addModule(DefaultScalaModule)
      .build()

}
