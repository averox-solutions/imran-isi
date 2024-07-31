package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.apps.RightsManagementTrait
import org.averox.core.db.{ JsonUtils, UserClientSettingsDAO, UserStateDAO }
import org.averox.core.models.Users2x
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }

trait SetUserClientSettingsReqMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetUserClientSettingsReqMsg(msg: SetUserClientSettingsReqMsg): Unit = {
    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      UserClientSettingsDAO.insertOrUpdate(liveMeeting.props.meetingProp.intId, user.intId, JsonUtils.mapToJson(msg.body.userClientSettingsJson))
    }
  }
}
