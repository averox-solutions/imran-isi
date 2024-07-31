package org.averox.core2.message.handlers.guests

import org.averox.common2.msgs.SetGuestPolicyCmdMsg
import org.averox.core.models.{ GuestPolicy, GuestPolicyType, GuestsWaiting }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.running.MeetingActor

trait SetGuestPolicyMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleSetGuestPolicyMsg(msg: SetGuestPolicyCmdMsg): Unit = {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) || liveMeeting.props.meetingProp.isBreakout) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to set guest policy in meeting."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      val newPolicy = msg.body.policy.toUpperCase()
      if (GuestPolicyType.policyTypes.contains(newPolicy)) {
        val policy = GuestPolicy(newPolicy, msg.body.setBy)
        GuestsWaiting.setGuestPolicy(
          liveMeeting.props.meetingProp.intId,
          liveMeeting.guestsWaiting,
          policy
        )
        val event = MsgBuilder.buildGuestPolicyChangedEvtMsg(
          liveMeeting.props.meetingProp.intId, msg.header.userId, newPolicy, msg.body.setBy
        )
        outGW.send(event)
      }
    }
  }

}