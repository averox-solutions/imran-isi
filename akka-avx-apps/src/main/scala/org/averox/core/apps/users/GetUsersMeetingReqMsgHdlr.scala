package org.averox.core.apps.users

import org.averox.common2.msgs.GetUsersMeetingReqMsg
import org.averox.core.running.{ HandlerHelpers, LiveMeeting, OutMsgRouter }

trait GetUsersMeetingReqMsgHdlr extends HandlerHelpers {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleGetUsersMeetingReqMsg(msg: GetUsersMeetingReqMsg): Unit = {
    sendAllUsersInMeeting(msg.body.userId)
    sendAllVoiceUsersInMeeting(msg.body.userId, liveMeeting.voiceUsers, liveMeeting.props.meetingProp.intId)
  }
}
