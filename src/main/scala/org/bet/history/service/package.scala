package org.bet.history

import akka.util.Timeout

import scala.concurrent.duration._

package object service {

  implicit val timeout: Timeout = 3.seconds

}
