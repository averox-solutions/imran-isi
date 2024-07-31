package org.averox.core.apps.users

import org.averox.common2.msgs.UserActivitySignCmdMsg
import org.averox.core.models.Users2x
import org.averox.core.running.MeetingActor

trait UserActivitySignCmdMsgHdlr {
  this: MeetingActor =>

  def handleUserActivitySignCmdMsg(msg: UserActivitySignCmdMsg): Unit = {
    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
    } yield {
      Users2x.updateLastUserActivity(liveMeeting.users2x, user)
    }
  }
}
