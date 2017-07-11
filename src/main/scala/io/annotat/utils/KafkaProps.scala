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

package io.annotat.utils

import java.security.InvalidParameterException
import java.util.Properties

import scala.util.matching.Regex

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 7/6/17
  */
class KafkaProps(host: String, port: String, groupId: String, topic: String) {

  def getProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", s"$host:$port")
    props.put("group.id", groupId)
    props.put("client.id", "PantaRhei")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  def getTopic: String = topic

}

object KafkaProps {

  private val PropsPattern: Regex = """^([-0-9a-zA-Z.]+):(\d{4,5}):([-0-9a-zA-Z.]+):([-0-9a-zA-Z.]+)$""".r

  def apply(s: String): KafkaProps = s match {
    case PropsPattern(h, p, g, t) => new KafkaProps(h, p, g, t)
    case x => throw new InvalidParameterException(
      s"$x is not a valid parameter. Has to be in the form of <host>:<port>:<groupId>:<topic>"
    )
  }

}
