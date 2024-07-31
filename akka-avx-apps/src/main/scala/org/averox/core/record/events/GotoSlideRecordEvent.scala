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

class GotoSlideRecordEvent extends AbstractPresentationRecordEvent {
  import GotoSlideRecordEvent._

  setEvent("GotoSlideEvent")

  def setPresentationName(name: String) {
    eventMap.put(PRES_NAME, name)
  }

  def setSlide(slide: Integer) {
    /*
     * Subtract 1 from the page number to be zero-based to be
     * compatible with 0.81 and earlier. (ralam Sept 2, 2014)
     */
    eventMap.put(SLIDE, Integer.toString(slide - 1))
  }

  def setId(id: String) {
    eventMap.put(ID, id)
  }
}

object GotoSlideRecordEvent {
  protected final val PRES_NAME = "presentationName"
  protected final val SLIDE = "slide"
  protected final val ID = "id"
}