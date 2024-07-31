package org.averox.core.apps.users

import org.averox.common2.msgs._
import org.averox.core.models.{ UserState, Users2x }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.domain.MeetingState2x

trait SetUserSpeechOptionsMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetUserSpeechOptionsReqMsg(msg: SetUserSpeechOptionsReqMsg): Unit = {
    log.info("handleSetUserSpeechOptionsReqMsg: partialUtterances={} minUtteranceLength={} userId={}", msg.body.partialUtterances, msg.body.minUtteranceLength, msg.header.userId)

    def broadcastUserSpeechOptionsChanged(user: UserState, partialUtterances: Boolean, minUtteranceLength: Int): Unit = {
      val routingChange = Routing.addMsgToClientRouting(
        MessageTypes.BROADCAST_TO_MEETING,
        liveMeeting.props.meetingProp.intId, user.intId
      )
      val envelopeChange = BbbCoreEnvelope(UserSpeechOptionsChangedEvtMsg.NAME, routingChange)
      val headerChange = BbbClientMsgHeader(UserSpeechOptionsChangedEvtMsg.NAME, liveMeeting.props.meetingProp.intId, user.intId)

      val bodyChange = UserSpeechOptionsChangedEvtMsgBody(partialUtterances, minUtteranceLength)
      val eventChange = UserSpeechOptionsChangedEvtMsg(headerChange, bodyChange)
      val msgEventChange = BbbCommonEnvCoreMsg(envelopeChange, eventChange)
      outGW.send(msgEventChange)
    }

    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      broadcastUserSpeechOptionsChanged(user, msg.body.partialUtterances, msg.body.minUtteranceLength)
    }

  }
}
