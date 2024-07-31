package org.averox.core.apps.presentationpod

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.db.PresPresentationDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.PresentationInPod
import org.averox.core.running.LiveMeeting

trait PresentationHasInvalidMimeTypeErrorPubMsgHdlr {
  this: PresentationPodHdlrs =>

  def handle(
      msg: PresentationHasInvalidMimeTypeErrorSysPubMsg, state: MeetingState2x,
      liveMeeting: LiveMeeting, bus: MessageBus
  ): MeetingState2x = {

    def broadcastEvent(msg: PresentationHasInvalidMimeTypeErrorSysPubMsg): Unit = {
      val routing = Routing.addMsgToClientRouting(
        MessageTypes.BROADCAST_TO_MEETING,
        liveMeeting.props.meetingProp.intId, msg.header.userId
      )
      val envelope = BbbCoreEnvelope(PresentationHasInvalidMimeTypeErrorEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(
        PresentationHasInvalidMimeTypeErrorEvtMsg.NAME,
        liveMeeting.props.meetingProp.intId, msg.header.userId
      )

      val body = PresentationHasInvalidMimeTypeErrorEvtMsgBody(msg.body.podId, msg.body.meetingId,
        msg.body.presentationName, msg.body.temporaryPresentationId,
        msg.body.presentationId, msg.body.messageKey, msg.body.fileMime, msg.body.fileExtension)
      val event = PresentationHasInvalidMimeTypeErrorEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    val errorDetails = scala.collection.immutable.Map(
      "fileMime" -> msg.body.fileMime,
      "fileExtension" -> msg.body.fileExtension
    )

    PresPresentationDAO.updateErrors(msg.body.presentationId, msg.body.messageKey, errorDetails)
    broadcastEvent(msg)

    state
  }
}