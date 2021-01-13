package org.bet.history.service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

abstract class RepositoryService[T, K](items: List[T], keySelector: T => K) {

  private val cache: Map[K, T] = items.map(item => keySelector(item) -> item).toMap

  def getItem[E](id: K, serviceError: => E): Future[Either[E, T]] = Future {
    cache.get(id) match {
      case Some(item) =>
        Right(item)
      case None =>
        Left(serviceError)
    }
  }

}

object RepositoryService {

  def readResource[T](inputFilePath: String)(parseLine: String => T): List[T] = {
    val source = Source fromResource inputFilePath
    val items = source.getLines().drop(1).map(parseLine).toList

    source.close()
    items
  }

}
