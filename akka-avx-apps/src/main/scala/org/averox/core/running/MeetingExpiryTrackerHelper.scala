package org.averox.core.running

import org.averox.common2.msgs._
import org.averox.core.bus.InternalEventBus
import org.averox.core.domain.{ MeetingEndReason, MeetingState2x }
import org.averox.core.util.TimeUtil

trait MeetingExpiryTrackerHelper extends HandlerHelpers {

  def processMeetingExpiryAudit(
      outGW:       OutMsgRouter,
      eventBus:    InternalEventBus,
      liveMeeting: LiveMeeting,
      state:       MeetingState2x
  ): (MeetingState2x, Option[String]) = {
    val nowInMs = TimeUtil.timeNowInMs()

    val (expired, reason) = state.expiryTracker.hasMeetingExpired(nowInMs)
    if (expired) {
      for {
        expireReason <- reason
      } yield {
        endAllBreakoutRooms(eventBus, liveMeeting, state, expireReason)
        sendEndMeetingDueToExpiry(expireReason, eventBus, outGW, liveMeeting, "system")
      }
    }

    (state, reason)
  }
}
