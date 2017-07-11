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

import akka.actor.SupervisorStrategy.{Escalate, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import io.annotat.actors.Input.GetContent
import io.annotat.actors.InputManager.HarvestContent
import io.annotat.utils.LogMessages._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.matching.Regex

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 6/29/17
  */
class InputManager(emitter: ActorRef) extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 1 minute) {
      case mue: MalformedURLException =>
        log.error(mue.getMessage)
        Stop
      case uhe: UnknownHostException =>
        log.error(uhe.getMessage)
        Stop
      case _: Exception =>
        Escalate
    }

  var contentList: List[Iterator[String]] = List()

  override def preStart(): Unit = log.info(actorStartMsg(self.path.name))

  override def postStop(): Unit = log.info(actorStopMsg(self.path.name))

  override def receive: Receive = {
    case HarvestContent(sources, tokenizer) =>
      for (s <- sources) {
        if (Files.isDirectory(Paths.get(s))) {
          Files
            .walk(Paths.get(s))
            .forEach(x => context.actorOf(Input.props(emitter), x.hashCode().toString) ! GetContent(x.toString, tokenizer))
        } else {
          val inputActorRef = context.actorOf(Input.props(emitter), s.hashCode().toString)
          inputActorRef ! GetContent(s, tokenizer)
        }
      }
  }
}

object InputManager {
  def props(emitter: ActorRef): Props = Props(new InputManager(emitter))

  final case class HarvestContent(sources: Seq[String], tokenizer: Regex)

}
