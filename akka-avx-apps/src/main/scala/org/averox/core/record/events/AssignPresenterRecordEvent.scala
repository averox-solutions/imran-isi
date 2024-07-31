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

class AssignPresenterRecordEvent extends AbstractParticipantRecordEvent {
  import AssignPresenterRecordEvent._

  setEvent("AssignPresenterEvent")

  def setUserId(userid: String) {
    eventMap.put(USER_ID, userid)
  }

  def setName(name: String) {
    eventMap.put(NAME, name)
  }

  def setAssignedBy(by: String) {
    eventMap.put(ASSIGNED_BY, by)
  }
}

object AssignPresenterRecordEvent {
  protected final val USER_ID = "userid"
  protected final val NAME = "name"
  protected final val ASSIGNED_BY = "assignedBy"
}