package org.averox.core.apps.presentationpod

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.running.LiveMeeting

trait PresentationUploadedFileTooLargeErrorPubMsgHdlr {
  this: PresentationPodHdlrs =>

  def handle(
      msg: PresentationUploadedFileTooLargeErrorSysPubMsg, state: MeetingState2x,
      liveMeeting: LiveMeeting, bus: MessageBus
  ): MeetingState2x = {

    def broadcastEvent(msg: PresentationUploadedFileTooLargeErrorSysPubMsg): Unit = {
      val routing = Routing.addMsgToClientRouting(
        MessageTypes.BROADCAST_TO_MEETING,
        liveMeeting.props.meetingProp.intId, msg.header.userId
      )
      val envelope = BbbCoreEnvelope(PresentationUploadedFileTooLargeErrorEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(
        PresentationUploadedFileTooLargeErrorEvtMsg.NAME,
        liveMeeting.props.meetingProp.intId, msg.header.userId
      )

      val body = PresentationUploadedFileTooLargeErrorEvtMsgBody(msg.body.podId, msg.body.messageKey, msg.body.code, msg.body.presentationName, msg.body.presentationToken, msg.body.fileSize, msg.body.maxFileSize)
      val event = PresentationUploadedFileTooLargeErrorEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    broadcastEvent(msg)
    state
  }
}
