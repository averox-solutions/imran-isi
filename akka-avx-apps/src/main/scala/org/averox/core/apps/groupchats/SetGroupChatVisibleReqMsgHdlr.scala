package org.averox.core.apps.groupchats

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.ChatUserDAO
import org.averox.core.models.Users2x
import org.averox.core.running.{ LiveMeeting, LogHelper }

trait SetGroupChatVisibleReqMsgHdlr {
  def handle(msg: SetGroupChatVisibleReqMsg, liveMeeting: LiveMeeting): Unit = {
    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      ChatUserDAO.updateChatVisible(liveMeeting.props.meetingProp.intId, msg.body.chatId, user.intId, msg.body.visible)
    }
  }
}
