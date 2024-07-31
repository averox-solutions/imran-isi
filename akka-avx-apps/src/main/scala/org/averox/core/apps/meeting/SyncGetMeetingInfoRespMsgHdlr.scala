package org.averox.core.apps.meeting

import org.averox.common2.domain.DefaultProps
import org.averox.common2.msgs._
import org.averox.core.running.OutMsgRouter

trait SyncGetMeetingInfoRespMsgHdlr {

  val outGW: OutMsgRouter

  def handleSyncGetMeetingInfoRespMsg(props: DefaultProps): Unit = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, props.meetingProp.intId, "nodeJSapp")
    val envelope = BbbCoreEnvelope(SyncGetMeetingInfoRespMsg.NAME, routing)
    val header = BbbCoreBaseHeader(SyncGetMeetingInfoRespMsg.NAME)

    val body = SyncGetMeetingInfoRespMsgBody(props)
    val event = SyncGetMeetingInfoRespMsg(header, body)
    val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
    outGW.send(msgEvent)
  }
}
