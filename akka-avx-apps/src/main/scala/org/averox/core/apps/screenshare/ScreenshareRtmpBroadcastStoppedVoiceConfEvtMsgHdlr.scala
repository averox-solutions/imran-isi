package org.averox.core.apps.screenshare

import org.averox.common2.msgs._
import org.averox.core.apps.ScreenshareModel
import org.averox.core.apps.ScreenshareModel.getRTMPBroadcastingUrl
import org.averox.core.bus.MessageBus
import org.averox.core.running.LiveMeeting
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.apps.screenshare.ScreenshareApp2x.broadcastStopped
import org.averox.core.db.ScreenshareDAO

trait ScreenshareRtmpBroadcastStoppedVoiceConfEvtMsgHdlr {
  this: ScreenshareApp2x =>

  def handle(msg: ScreenshareRtmpBroadcastStoppedVoiceConfEvtMsg, liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    log.info("handleScreenshareRTMPBroadcastStoppedRequest: isBroadcastingRTMP=" +
      ScreenshareModel.isBroadcastingRTMP(liveMeeting.screenshareModel) + " URL:" +
      ScreenshareModel.getRTMPBroadcastingUrl(liveMeeting.screenshareModel))

    ScreenshareDAO.updateStopped(liveMeeting.props.meetingProp.intId, getRTMPBroadcastingUrl(liveMeeting.screenshareModel))

    broadcastStopped(bus.outGW, liveMeeting)
  }
}
