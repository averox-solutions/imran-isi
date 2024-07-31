package org.averox.core.apps

import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.{ MsgBuilder }

object ExternalVideoModel {
  def setURL(externalVideoModel: ExternalVideoModel, externalVideoUrl: String) {
    externalVideoModel.externalVideoUrl = externalVideoUrl
  }

  def clear(externalVideoModel: ExternalVideoModel) {
    externalVideoModel.externalVideoUrl = ""
  }

  def stop(outGW: OutMsgRouter, liveMeeting: LiveMeeting) {
    if (!liveMeeting.externalVideoModel.externalVideoUrl.isEmpty) {
      liveMeeting.externalVideoModel.externalVideoUrl = ""

      val event = MsgBuilder.buildStopExternalVideoEvtMsg(liveMeeting.props.meetingProp.intId)
      outGW.send(event)
    }
  }
}

class ExternalVideoModel {
  private var externalVideoUrl = ""
}
