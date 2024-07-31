package org.averox.core.apps.chat

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.ChatUserDAO
import org.averox.core.running.{ LiveMeeting, LogHelper }

trait UserTypingPubMsgHdlr extends LogHelper {
  def handle(msg: UserTypingPubMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    def broadcastEvent(msg: UserTypingPubMsg): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(UserTypingEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(UserTypingEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = UserTypingEvtMsgBody(msg.body.chatId, msg.header.userId)
      val event = UserTypingEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }
    ChatUserDAO.updateUserTyping(liveMeeting.props.meetingProp.intId, msg.body.chatId, msg.header.userId)
    broadcastEvent(msg)
  }
}
