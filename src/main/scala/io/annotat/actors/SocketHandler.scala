/*
 *     <one line to give the program's name and a brief idea of what it does.>
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

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.ByteString
import io.annotat.actors.Emitter.Push
import io.annotat.utils.LogMessages._

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 7/10/17
  */
class SocketHandler(bound: ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info(actorStartMsg(self.path.name))

  override def postStop(): Unit = log.info(actorStopMsg(self.path.name))

  override def receive: Receive = {
    case Push(msg) =>
      bound ! Write(ByteString(msg))
  }

}

object SocketHandler {

  def props(bound: ActorRef): Props = Props(new SocketHandler(bound))

}
