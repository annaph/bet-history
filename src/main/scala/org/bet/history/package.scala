package org.bet

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}

package object history {

  private val dateTimeFormatter = DateTimeFormatter ofPattern "yyyy-MM-dd HH:mm:ss"

  implicit class StringOps(str: String) {

    def toInstant: Instant =
      LocalDateTime.parse(str, dateTimeFormatter).atOffset(ZoneOffset.UTC).toInstant

  }

}
