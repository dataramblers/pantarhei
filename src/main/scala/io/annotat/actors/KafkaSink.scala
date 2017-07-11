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

import java.time.LocalDate
import java.util.Properties

import akka.actor.{Actor, ActorLogging, Props}
import io.annotat.actors.Emitter.Push
import io.annotat.utils.LogMessages._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 6/29/17
  */
class KafkaSink(kafkaProducer: KafkaProducer[String, String], kafkaTopic: String) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(actorStartMsg(self.path.name))
  }

  override def postStop(): Unit = {
    kafkaProducer.close()
    log.info(actorStopMsg(self.path.name))
  }

  override def receive: Receive = {
    case Push(msg) =>
      val localDate = LocalDate.now().toString
      kafkaProducer.send(new ProducerRecord[String, String](kafkaTopic, localDate, msg))
  }

}

object KafkaSink {
  def props(kafkaProps: Properties, topic: String): Props = {
    val producer = new KafkaProducer[String, String](kafkaProps)
    Props(new KafkaSink(producer, topic))
  }


}
