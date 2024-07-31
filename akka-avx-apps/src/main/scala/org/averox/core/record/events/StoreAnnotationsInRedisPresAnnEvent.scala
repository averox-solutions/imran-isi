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

import org.averox.common2.msgs.{ AnnotationVO, ExportJob, StoredAnnotations, PresentationPageForExport }
import org.averox.common2.util.JsonUtil

class StoreAnnotationsInRedisPresAnnEvent extends AbstractPresentationWithAnnotations {
  import StoreAnnotationsInRedisPresAnnEvent._

  setEvent("StoreAnnotationsInRedisPresAnnEvent")

  def setJobId(jobId: String) {
    eventMap.put(JOB_ID, jobId)
  }

  def setPresId(presId: String) {
    eventMap.put(PRES_ID, presId)
  }

  def setPages(pages: List[PresentationPageForExport]) {
    eventMap.put(PAGES, JsonUtil.toJson(pages))
  }
}

object StoreAnnotationsInRedisPresAnnEvent {
  protected final val JOB_ID = "jobId"
  protected final val PRES_ID = "presId"
  protected final val PAGES = "pages"
}

