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

import scala.util.matching.Regex


/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 7/2/17
  */
object ArgsParser {

  val parser = new scopt.OptionParser[ArgsConfig]("pantarhei") {

    head("PantaRhei", "0.1")

    opt[String]('i', "inputs")
      .required()
      .action((x, c) => c.copy(input = c.input :+ x))
      .unbounded()
      .text("Input files / URLs / directories to process. Argument can be used several times.")

    opt[String]('t', "token")
      .optional()
      .validate {
        case "sentence" => success
        case "line" => success
        case "word" => success
        case "char" => success
        case _ => failure("Value not one of 'sentence', 'line', 'word', 'char'.")
      }
      .action((x, c) => c.copy(token = x match {
        case "sentence" => Tokenizer("""\.[\n\r ]""".r, ".", " ")
        case "line" => Tokenizer("""[\n\r]\n?""".r, "", "\n")
        case "word" => Tokenizer("""\s+""".r, "", " ")
        case "char" => Tokenizer(".".r, "", "")
      }))
      .text("Tokens to be extracted: One of 'sentence', 'line', 'word', 'char'. Defaults to 'sentence'.")

    opt[String]('k', "kafka")
      .optional()
      .action((x, c) => c.copy(kafka = Some(KafkaProps(x))))
      .text("Kafka sink. Value must match <host>:<port>:<groupId>:<topic>. Off per default")

    opt[Int]('s', "socket")
      .optional()
      .action((x, c) => c.copy(socket = Some(x)))
      .text("Connects to socket. Value must match <port>. Off per default")

    opt[Unit]('o', "nostdout")
      .optional()
      .action((_, c) => c.copy(stdout = false))
      .text("Suppress output to STDOUT. Off per default")

    opt[String]('e', "emitFreq")
      .optional()
      .action((x, c) => c.copy(emitFreq = RangeBoundaries(x)))
      .text("Emission frequency in milliseconds. Exact value or range (<ms>-<ms>). Defaults to 1000")

    opt[String]('n', "tokenNumber")
      .optional()
      .action((x, c) => c.copy(tokenNumber = RangeBoundaries(x)))
      .text("Number of tokens per emission. Exact value or range (<tpe>-<tpe>). Defaults to 1")

    help("help").text("Prints this help text.")

    checkConfig(c =>
      if (!c.stdout && c.kafka.isEmpty && c.socket.isEmpty)
        failure("No output sink defined!")
      else success)

  }

  case class ArgsConfig(input: Seq[String] = Seq(),
                        token: Tokenizer = Tokenizer("""\.[\n\r ]""".r, ".", " "),
                        stdout: Boolean = true,
                        socket: Option[Int] = None,
                        kafka: Option[KafkaProps] = None,
                        emitFreq: RangeBoundaries = RangeBoundaries(1000, 1001),
                        tokenNumber: RangeBoundaries = RangeBoundaries(1, 2))

  case class Tokenizer(splitter: Regex = """\.[\n\r]""".r,
                       postfix: String,
                       joint: String)

}
