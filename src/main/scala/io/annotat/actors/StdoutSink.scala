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

import akka.actor.{Actor, ActorLogging, Props}
import io.annotat.actors.Emitter.Push
import io.annotat.utils.LogMessages._

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 6/29/17
  */
class StdoutSink extends Actor with ActorLogging {

  override def preStart(): Unit = log.info(actorStartMsg(self.path.name))

  override def postStop(): Unit = log.info(actorStopMsg(self.path.name))

  override def receive: Receive = {
    case Push(msg) =>
      log.debug("Send message \"{}\" to STDOUT", msg)
      print(msg)
  }
}

object StdoutSink {

  def props: Props = Props[StdoutSink]

}
