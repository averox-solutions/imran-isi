package org.averox.core.apps.webcam

import org.averox.common2.msgs._
import org.averox.core.bus.MessageBus
import org.averox.core.models.Webcams
import org.averox.core.running.LiveMeeting

trait CamStreamUnsubscribedInSfuEvtMsgHdlr {
  this: WebcamApp2x =>

  def handle(
      msg:         CamStreamUnsubscribedInSfuEvtMsg,
      liveMeeting: LiveMeeting,
      bus:         MessageBus
  ) {

    Webcams.removeSubscriber(
      liveMeeting.webcams,
      msg.body.streamId,
      msg.header.userId
    )
  }
}
