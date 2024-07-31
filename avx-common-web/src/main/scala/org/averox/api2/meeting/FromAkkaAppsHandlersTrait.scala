package org.averox.api2.meeting

import org.averox.common2.msgs.MeetingCreatedEvtMsg

trait FromAkkaAppsHandlersTrait {
  def handleMeetingCreatedEvtMsg(msg: MeetingCreatedEvtMsg): Unit = {
    println("************* HANDLING " + msg.header.name)
  }
}
