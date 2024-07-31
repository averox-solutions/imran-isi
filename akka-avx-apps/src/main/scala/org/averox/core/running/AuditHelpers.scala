package org.averox.core.running

import org.averox.common2.domain.DefaultProps
import org.averox.common2.msgs._
import org.averox.core.bus.{ InternalEventBus }

trait AuditHelpers {

  def getUsersInVoiceConf(
      props: DefaultProps,
      outGW: OutMsgRouter
  ): Unit = {
    def buildGetUsersInVoiceConfSysMsg(meetingId: String): BbbCommonEnvCoreMsg = {
      val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
      val envelope = BbbCoreEnvelope(GetUsersInVoiceConfSysMsg.NAME, routing)
      val body = GetUsersInVoiceConfSysMsgBody(props.voiceProp.voiceConf)
      val header = BbbCoreHeaderWithMeetingId(GetUsersInVoiceConfSysMsg.NAME, meetingId)
      val event = GetUsersInVoiceConfSysMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    val event = buildGetUsersInVoiceConfSysMsg(props.meetingProp.intId)
    outGW.send(event)
  }

  def sendBreakoutRoomCreatedToParent(
      props:    DefaultProps,
      eventBus: InternalEventBus
  ): Unit = {
    //    eventBus.publish(AveroxEvent(
    //      props.breakoutProps.parentId,
    //      BreakoutRoomCreated(props.breakoutProps.parentId, props.meetingProp.intId)
    //    ))
  }

}
