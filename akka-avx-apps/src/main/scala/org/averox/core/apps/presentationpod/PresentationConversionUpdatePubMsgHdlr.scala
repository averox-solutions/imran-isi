package org.averox.core.apps.presentationpod

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.PresPresentationDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.PresentationInPod
import org.averox.core.running.LiveMeeting

trait PresentationConversionUpdatePubMsgHdlr {
  this: PresentationPodHdlrs =>

  def handle(msg: PresentationConversionUpdateSysPubMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus): MeetingState2x = {

    val presentationId = msg.body.presentationId
    val pres = new PresentationInPod(presentationId, msg.body.presName, default = false, current = false, Map.empty, downloadable = false,
      "", removable = true, filenameConverted = msg.body.presName, uploadCompleted = false, numPages = 0, errorDetails = Map.empty)

    PresPresentationDAO.updateConversionStarted(liveMeeting.props.meetingProp.intId, pres)

    state
  }
}
