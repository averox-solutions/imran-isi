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

trait AbstractPresentationRecordEvent extends RecordEvent {
  import AbstractPresentationRecordEvent._

  setModule("PRESENTATION")

  def setPodId(id: String) {
    eventMap.put(POD_ID, id)
  }
}

object AbstractPresentationRecordEvent {
  protected final val POD_ID = "podId"
}
