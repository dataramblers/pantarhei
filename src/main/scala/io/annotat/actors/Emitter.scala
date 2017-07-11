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

import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Properties

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import io.annotat.actors.Emitter._
import io.annotat.utils.LogMessages._
import io.annotat.utils.RangeBoundaries

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 6/29/17
  */
class Emitter(emitFreq: RangeBoundaries, tokenNumber: RangeBoundaries, postfix: String, joint: String) extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 1 minute) {
      case _: SocketTimeoutException =>
        log.error("SocketTimeoutException in actor {}. Restarting", sender.path.name)
        Restart
      case _: IOException =>
        log.error("IOException in actor {}. Restarting", sender.path.name)
        Restart
      case _ =>
        Escalate
    }

  var sinks: List[ActorRef] = List[ActorRef]()

  override def preStart(): Unit = log.info(actorStartMsg(self.path.name))

  override def postStop(): Unit = log.info(actorStopMsg(self.path.name))

  override def receive: Receive = {
    case TokenizedContent(contentList) =>
      log.info("Tokenized resource from actor {} received", sender.path.name)
      contentList.map(x => x + postfix).foldLeft((tokenNumber.getInt, Queue[String]()))((agg, x) => {
        if (agg._2.size == agg._1) {
          sinks.foreach(_ ! Push(agg._2.mkString))
          Thread.sleep(emitFreq.getInt)
          (tokenNumber.getInt, Queue[String]())
        } else {
          (agg._1, agg._2.enqueue(if (agg._2.isEmpty) x else joint + x))
        }
      })
    case SetupStdoutSink =>
      sinks = context.actorOf(StdoutSink.props, "stdoutSink") :: sinks
    case SetupKafkaSink(p, t) =>
      sinks = context.actorOf(KafkaSink.props(p, t), "kafkaSink") :: sinks
    case SetupSocketSink(p) =>
      context.actorOf(SocketServerSink.props(p), "socketServer") :: sinks
    case SetTcpServer(b) =>
      sinks = context.actorOf(SocketHandler.props(b), "socketSink") :: sinks

  }

}

object Emitter {

  def props(emitFreq: RangeBoundaries, tokenNumber: RangeBoundaries, postfix: String, joint: String): Props = Props(new Emitter(emitFreq, tokenNumber, postfix, joint))

  final case class TokenizedContent(content: List[String])

  final case class SetupKafkaSink(kafkaProps: Properties, topic: String)

  final case class SetupSocketSink(port: Int)

  final case class Push(msg: String)

  final case class SetTcpServer(b: ActorRef)

  final case object SetupStdoutSink

  final case object Emit

}
