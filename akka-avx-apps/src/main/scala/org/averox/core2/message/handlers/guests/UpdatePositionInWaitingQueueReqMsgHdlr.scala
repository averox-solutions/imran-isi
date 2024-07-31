package org.averox.core2.message.handlers.guests

import org.averox.common2.msgs.UpdatePositionInWaitingQueueReqMsg
import org.averox.core.models.{ GuestsWaiting }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.running.MeetingActor

trait UpdatePositionInWaitingQueueReqMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  //This class could be used for logging the information passed

  def handleUpdatePositionInWaitingQueueReqMsg(msg: UpdatePositionInWaitingQueueReqMsg): Unit = {
    val event = MsgBuilder.buildPosInWaitingQueueUpdatedRespMsg(
      liveMeeting.props.meetingProp.intId,
      msg.body.guests
    )
    outGW.send(event)
  }

}
