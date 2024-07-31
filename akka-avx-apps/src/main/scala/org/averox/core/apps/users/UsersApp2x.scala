package org.averox.core.apps.users

import org.averox.core.running.MeetingActor

trait UsersApp2x
  extends UserLeaveReqMsgHdlr
  with LockUserInMeetingCmdMsgHdlr
  with LockUsersInMeetingCmdMsgHdlr
  with GetLockSettingsReqMsgHdlr
  with ClearAllUsersReactionCmdMsgHdlr {

  this: MeetingActor =>

}
