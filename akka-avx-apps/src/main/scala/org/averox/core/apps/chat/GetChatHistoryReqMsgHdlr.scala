package org.averox.core.apps.chat

import org.averox.common2.msgs._
import org.averox.core.apps.ChatModel
import org.averox.core.bus.MessageBus
import org.averox.core.running.{ LiveMeeting, LogHelper }

trait GetChatHistoryReqMsgHdlr extends LogHelper {

  def handle(msg: GetChatHistoryReqMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.debug(" GOT CHAT HISTORY")

    def broadcastEvent(msg: GetChatHistoryReqMsg, history: Array[ChatMessageVO]): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(GetChatHistoryRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(GetChatHistoryRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = GetChatHistoryRespMsgBody(history)
      val event = GetChatHistoryRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    val history = ChatModel.getChatHistory(liveMeeting.chatModel)
    broadcastEvent(msg, history)
  }
}
