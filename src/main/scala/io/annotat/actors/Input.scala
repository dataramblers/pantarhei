/*
 *     pantarhei reads in arbitrary textual resources and emits them in tokenized chunks
 *     Copyright (C) 2017  Sebastian Schüpbach
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.annotat.actors

import java.net.{MalformedURLException, UnknownHostException}
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.annotat.actors.Emitter.TokenizedContent
import io.annotat.actors.Input.GetContent
import io.annotat.utils.LogMessages._

import scala.collection.immutable.Queue
import scala.io.{BufferedSource, Source}
import scala.util.matching.Regex

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 6/28/17
  */
class Input(emitter: ActorRef) extends Actor with ActorLogging {

  type BufferedStringList = (Queue[Char], List[String])

  val HttpPattern: Regex = "(^https?://.*)".r

  override def preStart(): Unit = log.info(actorStartMsg(self.path.name))

  override def postStop(): Unit = log.info(actorStopMsg(self.path.name))

  override def receive: Receive = {
    case GetContent(url, tokenizer) if HttpPattern.findFirstIn(url).isDefined =>
      try {
        log.info("Processing of remote resource at {} started.", url)
        val content = _tokenizeSource(url, Source.fromURL(_, "UTF-8"), tokenizer)
        log.info("Remote resource processed. Sending to {}.", emitter.path.name)
        emitter ! TokenizedContent(content)
        context.stop(self)
      }
      catch {
        case _: MalformedURLException => throw new MalformedURLException(s"URL $url is malformed")
        case _: UnknownHostException => throw new UnknownHostException(s"Host of URL $url is unknown")
      }
    case GetContent(file, tokenizer) if Files.isRegularFile(Paths.get(file)) =>
      try {
        log.info("Processing of local resource at {} started.", file)
        val content = _tokenizeSource(file, Source.fromFile(_, "UTF-8"), tokenizer)
        log.info("Remote resource processed. Sending to {}.", emitter.path.name)
        emitter ! TokenizedContent(content)
        context.stop(self)
      } catch {
        case _: Exception => throw new Exception(s"Could not retrieve file $file.")
      }
  }

  private def _tokenizeSource(source: String, extractionMethod: String => BufferedSource, tokenizer: Regex): List[String] = {
    extractionMethod(source)
      .foldLeft((Queue[Char](), List[String]()))((a, b) => _charStreamTokenizer(a, b, 5)(tokenizer))
      ._2
      .reverse
  }

  private def _charStreamTokenizer(agg: BufferedStringList, c: Char, bufferSize: Int)(tokenBoundary: Regex): (Queue[Char], List[String]) = {

    def fillQueue[A](q: Queue[A], elem: A)(size: Int): (Queue[A], Option[A]) = {
      if (q.size < size) (q.enqueue(elem), None)
      else {
        val qTmp = q.dequeue
        (qTmp._2.enqueue(elem), Some(qTmp._1))
      }
    }

    (agg, c) match {
      // 1. Fall: Liste ist leer
      case ((q: Queue[Char], l: List[String]), c: Char) if l.isEmpty =>
        val newQueueTuple = fillQueue(q, c)(bufferSize)
        newQueueTuple._2 match {
          case None => (newQueueTuple._1, l)
          case Some(c: Char) => (newQueueTuple._1, c.toString :: l)
        }
      // 2. Fall: Match
      case ((q: Queue[Char], l: List[String]), c: Char) =>
        val newQueueTuple = fillQueue(q, c)(bufferSize)
        tokenBoundary.findFirstMatchIn(newQueueTuple._1.mkString) match {
          case None =>
            newQueueTuple._2 match {
              case None => (newQueueTuple._1, l)
              case Some(c: Char) => (newQueueTuple._1, l.head + c.toString :: l.tail)
            }
          case Some(m: Regex.Match) =>
            (Queue[Char](), "" :: l.head + m.before :: l.tail)
        }
    }

  }

}

object Input {

  def props(emitter: ActorRef): Props = Props(new Input(emitter))

  final case class GetContent(url: String, tokenizer: Regex)

}

