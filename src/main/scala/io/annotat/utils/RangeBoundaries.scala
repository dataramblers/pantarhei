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

import scala.util.Random
import scala.util.matching.Regex

/**
  * @author Sebastian Schüpbach
  * @version 0.1
  *
  *          Created on 7/6/17
  */
class RangeBoundaries(start: Int, end: Int) {
  def getInt: Int = if (end == start + 1) start else Random.nextInt(end - start) + start
}

object RangeBoundaries {

  val DashPattern: Regex = """^(\d+)-(\d+)$""".r
  val NumberPattern: Regex = """^(\d+)$""".r

  def apply(s: String): RangeBoundaries = s match {
    case DashPattern(s: String, e: String) =>
      RangeBoundaries(s.toInt, e.toInt)
    case NumberPattern(p: String) =>
      RangeBoundaries(p.toInt, p.toInt + 1)
    case x => throw new InvalidParameterException(
      s"$x is not a valid parameter. Has to be in the form of <positive number> or <positive number>-<positive number>"
    )
  }

  def apply(s: Int, e: Int): RangeBoundaries = {
    if (_validate(s, e)) new RangeBoundaries(s, e) else throw new InvalidParameterException(s"$s must be greater than zero and smaller than $e")
    new RangeBoundaries(s, e)
  }

  private def _validate(s: Int, e: Int): Boolean = (s, e) match {
    case (s: Int, e: Int) if s >= e => false
    case (s: Int, _) if s < 1 => false
    case _ => true
  }

}
