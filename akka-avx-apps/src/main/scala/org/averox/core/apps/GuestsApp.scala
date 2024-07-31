package org.averox.core.apps

import org.averox.core.running.MeetingActor
import org.averox.core2.message.handlers.guests._

trait GuestsApp extends GetGuestsWaitingApprovalReqMsgHdlr
  with GuestsWaitingApprovedMsgHdlr
  with UpdatePositionInWaitingQueueReqMsgHdlr
  with SetGuestPolicyMsgHdlr
  with SetGuestLobbyMessageMsgHdlr
  with SetPrivateGuestLobbyMessageCmdMsgHdlr
  with GetGuestPolicyReqMsgHdlr {

  this: MeetingActor =>

}
