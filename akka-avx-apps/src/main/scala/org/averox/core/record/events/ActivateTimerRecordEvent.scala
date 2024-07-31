/**
 * Averox open source conferencing system - http://www.averox.org/
 *
 * Copyright (c) 2017 Averox Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * Averox is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Averox; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.averox.core.record.events

class ActivateTimerRecordEvent extends AbstractTimerRecordEvent {
  import ActivateTimerRecordEvent._

  setEvent("ActivateTimerEvent")

  def setStopwatch(value: Boolean) {
    eventMap.put(STOPWATCH, value.toString)
  }

  def setRunning(value: Boolean) {
    eventMap.put(RUNNING, value.toString)
  }

  def setTime(value: Int) {
    eventMap.put(TIME, value.toString)
  }

  def setAccumulated(value: Int) {
    eventMap.put(ACCUMULATED, value.toString)
  }

  def setTimestamp(value: Int) {
    eventMap.put(TIMESTAMP, value.toString)
  }

  def setTrack(value: String) {
    eventMap.put(TRACK, value)
  }
}

object ActivateTimerRecordEvent {
  protected final val STOPWATCH = "stopwatch"
  protected final val RUNNING = "running"
  protected final val TIME = "time"
  protected final val ACCUMULATED = "accumulated"
  protected final val TIMESTAMP = "timestamp"
  protected final val TRACK = "track"
}