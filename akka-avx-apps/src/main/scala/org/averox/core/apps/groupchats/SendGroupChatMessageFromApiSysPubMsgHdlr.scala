package org.averox.core.apps.groupchats

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.SystemUser
import org.averox.core.running.{ HandlerHelpers, LiveMeeting }

trait SendGroupChatMessageFromApiSysPubMsgHdlr extends HandlerHelpers {
  this: GroupChatHdlrs =>

  def handle(msg: SendGroupChatMessageFromApiSysPubMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus): MeetingState2x = {

    log.debug("RECEIVED SendGroupChatMessageFromApiSysPubMsg {}", msg)

    val chatDisabled: Boolean = liveMeeting.props.meetingProp.disabledFeatures.contains("chat")
    if (!chatDisabled) {
      val newState = for {
        sender <- GroupChatApp.findGroupChatUser(SystemUser.ID, liveMeeting.users2x)
        chat <- state.groupChats.find(GroupChatApp.MAIN_PUBLIC_CHAT)
      } yield {
        val groupChatMsgFromUser = GroupChatMsgFromUser(sender.id, sender.copy(name = msg.body.userName), msg.body.message)
        val gcm = GroupChatApp.toGroupChatMessage(sender.copy(name = msg.body.userName), groupChatMsgFromUser, emphasizedText = true)
        val gcs = GroupChatApp.addGroupChatMessage(liveMeeting.props.meetingProp.intId, chat, state.groupChats, gcm, GroupChatMessageType.API)

        val event = buildGroupChatMessageBroadcastEvtMsg(
          liveMeeting.props.meetingProp.intId,
          msg.body.userName, GroupChatApp.MAIN_PUBLIC_CHAT, gcm
        )

        bus.outGW.send(event)

        state.update(gcs)
      }

      newState match {
        case Some(ns) => ns
        case None     => state
      }
    } else {
      state
    }
  }

}
