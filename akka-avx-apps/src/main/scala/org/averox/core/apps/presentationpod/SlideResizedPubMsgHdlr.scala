package org.averox.core.apps.presentationpod

import org.averox.common2.msgs._
import org.averox.core.apps.RightsManagementTrait
import org.averox.core.bus.MessageBus
import org.averox.core.db.PresPageDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.LiveMeeting

trait SlideResizedPubMsgHdlr extends RightsManagementTrait {
  this: PresentationPodHdlrs =>

  def handle(msg: SlideResizedPubMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus) = {
    PresPageDAO.updateSlidePosition(msg.body.pageId, msg.body.width, msg.body.height,
      msg.body.xOffset, msg.body.yOffset, msg.body.widthRatio, msg.body.heightRatio)
    state
  }
}
