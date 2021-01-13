package org.bet.history

import org.bet.history.config.BetHistoryConfig
import org.springframework.boot.SpringApplication

object BetHistoryApp extends App {
  SpringApplication run classOf[BetHistoryConfig]
}
