package org.averox.core.apps.screenshare

import org.apache.pekko.actor.ActorContext
import org.apache.pekko.event.Logging
import org.averox.core.apps.ScreenshareModel
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder


object ScreenshareApp2x {
  def requestBroadcastStop(outGW: OutMsgRouter, liveMeeting: LiveMeeting): Unit = {
    if (ScreenshareModel.isBroadcastingRTMP(liveMeeting.screenshareModel)) {
      val event = MsgBuilder.buildScreenBroadcastStopSysMsg(
        liveMeeting.props.meetingProp.intId,
        ScreenshareModel.getVoiceConf(liveMeeting.screenshareModel),
        ScreenshareModel.getRTMPBroadcastingUrl(liveMeeting.screenshareModel),
      )

      outGW.send(event)
    }
  }

  def broadcastStopped(outGW: OutMsgRouter, liveMeeting: LiveMeeting): Unit = {
    if (ScreenshareModel.isBroadcastingRTMP(liveMeeting.screenshareModel)) {
      ScreenshareModel.resetDesktopSharingParams(liveMeeting.screenshareModel)

      val event = MsgBuilder.buildStopScreenshareRtmpBroadcastEvtMsg(
        liveMeeting.props.meetingProp.intId,
        ScreenshareModel.getVoiceConf(liveMeeting.screenshareModel),
        ScreenshareModel.getScreenshareConf(liveMeeting.screenshareModel),
        ScreenshareModel.getRTMPBroadcastingUrl(liveMeeting.screenshareModel),
        ScreenshareModel.getScreenshareVideoWidth(liveMeeting.screenshareModel),
        ScreenshareModel.getScreenshareVideoHeight(liveMeeting.screenshareModel),
        ScreenshareModel.getTimestamp(liveMeeting.screenshareModel)
      )
      outGW.send(event)
    }
  }
}

class ScreenshareApp2x(implicit val context: ActorContext)
  extends GetScreenshareStatusReqMsgHdlr
  with ScreenshareRtmpBroadcastStartedVoiceConfEvtMsgHdlr
  with ScreenshareRtmpBroadcastStoppedVoiceConfEvtMsgHdlr
  with SyncGetScreenshareInfoRespMsgHdlr {

  val log = Logging(context.system, getClass)

}