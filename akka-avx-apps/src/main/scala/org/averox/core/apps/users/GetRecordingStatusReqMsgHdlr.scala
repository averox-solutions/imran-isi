package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.MeetingStatus2x

trait GetRecordingStatusReqMsgHdlr {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleGetRecordingStatusReqMsg(msg: GetRecordingStatusReqMsg) {

    def buildGetRecordingStatusRespMsg(
        meetingId:               String,
        userId:                  String,
        recorded:                Boolean,
        recording:               Boolean,
        recordFullDurationMedia: Boolean
    ): BbbCommonEnvCoreMsg = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
      val envelope = BbbCoreEnvelope(GetRecordingStatusRespMsg.NAME, routing)
      val body = GetRecordingStatusRespMsgBody(recorded, recording, recordFullDurationMedia, userId)
      val header = BbbClientMsgHeader(GetRecordingStatusRespMsg.NAME, meetingId, userId)
      val event = GetRecordingStatusRespMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    val event = buildGetRecordingStatusRespMsg(
      liveMeeting.props.meetingProp.intId,
      msg.body.requestedBy,
      liveMeeting.props.recordProp.record,
      MeetingStatus2x.isRecording(liveMeeting.status),
      liveMeeting.props.recordProp.recordFullDurationMedia
    )

    outGW.send(event)
  }
}
