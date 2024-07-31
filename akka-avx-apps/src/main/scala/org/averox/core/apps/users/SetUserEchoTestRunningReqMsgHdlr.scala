package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.apps.RightsManagementTrait
import org.averox.core.db.UserStateDAO
import org.averox.core.models.Users2x
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }

trait SetUserEchoTestRunningReqMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetUserEchoTestRunningReqMsg(msg: SetUserEchoTestRunningReqMsg): Unit = {
    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      UserStateDAO.updateEchoTestRunningAt(liveMeeting.props.meetingProp.intId, user.intId)
    }
  }
}
