package org.averox.core.apps.webcam

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.models.Webcams
import org.averox.core.running.LiveMeeting

trait CamStreamSubscribedInSfuEvtMsgHdlr {
  this: WebcamApp2x =>

  def handle(
      msg:         CamStreamSubscribedInSfuEvtMsg,
      liveMeeting: LiveMeeting,
      bus:         MessageBus
  ) {
    val meetingId = liveMeeting.props.meetingProp.intId

    val allowed = CameraHdlrHelpers.isCameraSubscribeAllowed(
      liveMeeting,
      meetingId,
      msg.header.userId,
      msg.body.streamId
    )

    if (allowed) {
      Webcams.addSubscriber(liveMeeting.webcams, msg.body.streamId, msg.header.userId)
    } else {
      CameraHdlrHelpers.requestCamSubscriptionEjection(
        meetingId,
        msg.header.userId,
        msg.body.streamId,
        bus.outGW
      )
    }
  }
}
