package org.averox.core.apps.polls

import org.averox.common2.domain.SimplePollResultOutVO
import org.averox.common2.msgs._
import org.averox.core.apps.groupchats.GroupChatApp
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.Polls
import org.averox.core.running.LiveMeeting
import org.averox.core.apps.{PermissionCheck, RightsManagementTrait}
import org.averox.core.db.{ChatMessageDAO, JsonUtils, NotificationDAO}
import org.averox.core2.message.senders.MsgBuilder
import spray.json.DefaultJsonProtocol.jsonFormat2

trait ShowPollResultReqMsgHdlr extends RightsManagementTrait {
  this: PollApp2x =>

  def handle(msg: ShowPollResultReqMsg, state: MeetingState2x, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastEvent(msg: ShowPollResultReqMsg, result: SimplePollResultOutVO, annot: AnnotationVO): Unit = {
      // PollShowResultEvtMsg
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(PollShowResultEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(PollShowResultEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = PollShowResultEvtMsgBody(msg.header.userId, msg.body.pollId, result)
      val event = PollShowResultEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)

      val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
        liveMeeting.props.meetingProp.intId,
        "info",
        "polling",
        "app.whiteboard.annotations.poll",
        "Message displayed when a poll is published",
        Vector()
      )
      bus.outGW.send(notifyEvent)
      NotificationDAO.insert(notifyEvent)

      // SendWhiteboardAnnotationPubMsg
      val annotationRouting = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val annotationEnvelope = BbbCoreEnvelope(SendWhiteboardAnnotationsEvtMsg.NAME, annotationRouting)
      val annotationHeader = BbbClientMsgHeader(SendWhiteboardAnnotationsEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val annotMsgBody = SendWhiteboardAnnotationsEvtMsgBody(annot.wbId, Array[AnnotationVO](annot))
      val annotationEvent = SendWhiteboardAnnotationsEvtMsg(annotationHeader, annotMsgBody)
      val annotationMsgEvent = BbbCommonEnvCoreMsg(annotationEnvelope, annotationEvent)
      bus.outGW.send(annotationMsgEvent)
    }

    if (permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to show poll results."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      for {
        (result, annotationProp) <- Polls.handleShowPollResultReqMsg(state, msg.header.userId, msg.body.pollId, liveMeeting)
      } yield {
        //it will be used to render the chat message (will be stored as json in chat-msg metadata)
        val resultAsSimpleMap = Map(
          "id" -> result.id,
          "questionType" -> result.questionType,
          "questionText" -> result.questionText.getOrElse(""),
          "answers" -> {
            for {
              answer <- result.answers
            } yield {
              Map(
                "id" -> answer.id,
                "key" -> answer.key,
                "numVotes" -> answer.numVotes
              )
            }
          },
          "numRespondents" -> result.numRespondents,
          "numResponders" -> result.numResponders,
        )

        ChatMessageDAO.insertSystemMsg(liveMeeting.props.meetingProp.intId, GroupChatApp.MAIN_PUBLIC_CHAT, "", GroupChatMessageType.POLL, resultAsSimpleMap, "")
        broadcastEvent(msg, result, annotationProp)
      }
    }
  }
}