package org.averox.core.apps.polls

import org.averox.common2.domain.SimplePollOutVO
import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.Polls
import org.averox.core.running.LiveMeeting
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }

trait StartCustomPollReqMsgHdlr extends RightsManagementTrait {
  this: PollApp2x =>

  def handle(msg: StartCustomPollReqMsg, state: MeetingState2x, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    def broadcastEvent(msg: StartCustomPollReqMsg, poll: SimplePollOutVO): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(PollStartedEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(PollStartedEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = PollStartedEvtMsgBody(msg.header.userId, poll.id, msg.body.pollType, msg.body.secretPoll, msg.body.question, poll)
      val event = PollStartedEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    if (permissionFailed(PermissionCheck.GUEST_LEVEL, PermissionCheck.PRESENTER_LEVEL, liveMeeting.users2x, msg.header.userId)) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to start custom poll."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW, liveMeeting)
    } else {
      for {
        pvo <- Polls.handleStartCustomPollReqMsg(state, msg.header.userId, msg.body.pollId, msg.body.pollType, msg.body.secretPoll, msg.body.isMultipleResponse, msg.body.answers, msg.body.question, liveMeeting)
      } yield {
        broadcastEvent(msg, pvo)
      }
    }
  }
}
