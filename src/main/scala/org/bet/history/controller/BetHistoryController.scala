package org.bet.history.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.bet.history.usecase.GetBetHistoryUseCase
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@RestController
class BetHistoryController(getBetHistoryUseCase: GetBetHistoryUseCase, objectMapper: ObjectMapper) {

  @RequestMapping(
    path = Array("/bets/{accountId}"),
    method = Array(RequestMethod.GET),
    produces = Array(MediaType.APPLICATION_JSON_VALUE))
  @ResponseBody
  def betHistory(@PathVariable("accountId") accountId: String,
                 @RequestParam(value = "first", defaultValue = "10") first: String,
                 @RequestParam(value = "after", required = false) after: String,
                 @RequestParam(value = "before", required = false) before: String): DeferredResult[ResponseEntity[String]] = {
    val deferredResult = new DeferredResult[ResponseEntity[String]]()

    getBetHistoryUseCase.betHistory(accountId, first, Option(after), Option(before))
      .map(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString)
      .onComplete {
        case Success(entityBody) =>
          val response = ResponseEntity.status(HttpStatus.OK).body(entityBody)
          deferredResult setResult response
        case Failure(e) =>
          e.printStackTrace()
          deferredResult.setErrorResult("An error occurred!")
      }

    deferredResult
  }

}

object BetHistoryController {

  def apply(getBetHistoryUseCase: GetBetHistoryUseCase, objectMapper: ObjectMapper): BetHistoryController =
    new BetHistoryController(getBetHistoryUseCase, objectMapper)

}
