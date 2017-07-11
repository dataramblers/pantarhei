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

package io.annotat

import akka.actor.ActorSystem
import io.annotat.actors.Turntable
import io.annotat.actors.Turntable.SetUpEnv
import io.annotat.utils.ArgsParser.{ArgsConfig, parser}

import scala.io.StdIn._


object PantaRhei extends App {
  parser.parse(args, ArgsConfig()) match {
    case Some(config) =>
      val system: ActorSystem = ActorSystem("PantaRhei")
      try {
        val turntable = system.actorOf(Turntable.props, "turntableActor")
        turntable ! SetUpEnv(config)
        readLine("Press Ctrl-X to interrupt")
      } finally {
        system.terminate()
      }
    case None => throw new Exception("Arguments are not valid")
  }
}
