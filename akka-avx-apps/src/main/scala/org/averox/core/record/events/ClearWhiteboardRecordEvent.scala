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

class ClearWhiteboardRecordEvent extends AbstractWhiteboardRecordEvent {
  import ClearWhiteboardRecordEvent._

  setEvent("ClearWhiteboardEvent")

  def setUserId(userId: String) {
    eventMap.put(USER_ID, userId)
  }

  def setFullClear(fullClear: Boolean) {
    eventMap.put(FULL_CLEAR, fullClear.toString)
  }
}

object ClearWhiteboardRecordEvent {
  protected final val USER_ID = "userId"
  protected final val FULL_CLEAR = "fullClear"
}