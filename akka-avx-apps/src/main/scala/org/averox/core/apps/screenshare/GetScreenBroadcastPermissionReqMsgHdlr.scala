package org.averox.core2.message.handlers

import org.averox.common2.msgs._
import org.averox.core.running.{ MeetingActor, OutMsgRouter }
import org.averox.core.models.Users2x
import org.averox.core.apps.PermissionCheck
import org.averox.core2.message.senders.MsgBuilder

trait GetScreenBroadcastPermissionReqMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleGetScreenBroadcastPermissionReqMsg(msg: GetScreenBroadcastPermissionReqMsg) {
    var allowed = false

    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
    } yield {
      if (permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL,
        liveMeeting.users2x, msg.header.userId)) {
        val meetingId = liveMeeting.props.meetingProp.intId
        val reason = "No permission to share the screen."
        PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
      } else if (liveMeeting.props.meetingProp.intId == msg.body.meetingId
        && liveMeeting.props.voiceProp.voiceConf == msg.body.voiceConf) {
        allowed = true
      }
    }

    val event = MsgBuilder.buildGetScreenBroadcastPermissionRespMsg(
      liveMeeting.props.meetingProp.intId,
      liveMeeting.props.voiceProp.voiceConf,
      msg.body.userId,
      msg.body.sfuSessionId,
      allowed
    )
    outGW.send(event)
  }
}
