package org.averox.core.apps.presentation

import org.averox.common2.domain.PageVO
import org.averox.common2.msgs.PreuploadedPresentationsSysPubMsg
import org.averox.core.apps.Presentation
import org.averox.core.bus.MessageBus
import org.averox.core.running.{ LiveMeeting }

trait PreuploadedPresentationsPubMsgHdlr {
  this: PresentationApp2x =>

  def handle(
      msg:         PreuploadedPresentationsSysPubMsg,
      liveMeeting: LiveMeeting, bus: MessageBus
  ): Unit = {

    val presos = new collection.mutable.HashMap[String, Presentation]

    msg.body.presentations.foreach { pres =>
      val pages = new collection.mutable.HashMap[String, PageVO]()

      pres.pages.foreach { p =>
        val page = new PageVO(p.id, p.num, p.thumbUri, p.txtUri, p.svgUri, p.current, p.xOffset, p.yOffset,
          p.widthRatio, p.heightRatio)
        pages += page.id -> page
      }

      val pr = new Presentation(pres.id, pres.name, pres.current,
        pages.toMap, pres.downloadable, pres.removable)
      presos += pres.id -> pr
    }

    processPreuploadedPresentations(liveMeeting, presos.values.toVector)

    msg.body.presentations foreach (presentation => {
      broadcastNewPresentationEvent(msg.header.userId, presentation, liveMeeting, bus)
    })
  }
}
