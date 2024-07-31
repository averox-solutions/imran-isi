package org.averox.core.apps.webcam

import org.averox.common2.msgs._
import org.averox.core.apps.PermissionCheck
import org.averox.core.bus.MessageBus
import org.averox.core.models.Webcams
import org.averox.core.running.LiveMeeting

trait UserBroadcastCamStopMsgHdlr {
  this: WebcamApp2x =>

  def handle(
      msg:         UserBroadcastCamStopMsg,
      liveMeeting: LiveMeeting,
      bus:         MessageBus
  ): Unit = {
    val meetingId = liveMeeting.props.meetingProp.intId
    val userId = msg.header.userId
    val streamId = msg.body.stream

    CameraHdlrHelpers.stopBroadcastedCam(
      liveMeeting,
      meetingId,
      userId,
      streamId,
      bus.outGW
    )
  }
}