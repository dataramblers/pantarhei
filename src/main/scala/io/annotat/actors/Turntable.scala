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

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import io.annotat.actors.Emitter.{SetupKafkaSink, SetupSocketSink, SetupStdoutSink}
import io.annotat.actors.InputManager.HarvestContent
import io.annotat.actors.Turntable.SetUpEnv
import io.annotat.utils.ArgsParser.ArgsConfig
import io.annotat.utils.KafkaProps
import io.annotat.utils.LogMessages._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 7/2/17
  */
class Turntable extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 1 minute) {
      case _: Exception =>
        Escalate
    }

  override def preStart(): Unit = log.info(actorStartMsg(self.path.name))

  override def postStop(): Unit = log.info(actorStopMsg(self.path.name))

  override def receive: Receive = {
    case SetUpEnv(c) =>
      log.info("Setting up pantarhei workflow")
      val emitter = context.actorOf(Emitter.props(c.emitFreq, c.tokenNumber, c.token.postfix, c.token.joint), "emitter")
      val inputManager = context.actorOf(InputManager.props(emitter), "inputManager")
      inputManager ! HarvestContent(c.input, c.token.splitter)
      if (c.stdout) emitter ! SetupStdoutSink
      c.kafka match {
        case Some(p: KafkaProps) => emitter ! SetupKafkaSink(p.getProps, p.getTopic)
        case None =>
      }
      c.socket match {
        case Some(i: Int) => emitter ! SetupSocketSink(i)
        case None =>
      }
  }

}

object Turntable {
  def props: Props = Props[Turntable]

  final case class SetUpEnv(config: ArgsConfig)

}
