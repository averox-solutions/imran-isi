package org.averox.core2.message.handlers.meeting

import org.averox.common2.msgs._
import org.averox.core.bus.InternalEventBus
import org.averox.core.domain.{ MeetingEndReason, MeetingState2x }
import org.averox.core.running.{ BaseMeetingActor, HandlerHelpers, LiveMeeting, OutMsgRouter }

trait EndMeetingSysCmdMsgHdlr extends HandlerHelpers {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter
  val eventBus: InternalEventBus

  def handleEndMeeting(msg: EndMeetingSysCmdMsg, state: MeetingState2x): Unit = {
    endAllBreakoutRooms(eventBus, liveMeeting, state, MeetingEndReason.ENDED_FROM_API)
    log.info("Meeting {} ended by from API.", msg.body.meetingId)
    sendEndMeetingDueToExpiry(MeetingEndReason.ENDED_FROM_API, eventBus, outGW, liveMeeting, "system")
  }

}
