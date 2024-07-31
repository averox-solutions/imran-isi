package org.averox.core2.message.handlers

import org.averox.common2.msgs._
import org.averox.core.models.{ UserState, Users2x, VoiceUserState, VoiceUsers }
import org.averox.core.running.{ MeetingActor, OutMsgRouter }
import org.averox.core2.MeetingStatus2x
import org.averox.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.averox.core.db.NotificationDAO
import org.averox.core2.message.senders.MsgBuilder

trait MuteAllExceptPresentersCmdMsgHdlr extends RightsManagementTrait {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleMuteAllExceptPresentersCmdMsg(msg: MuteAllExceptPresentersCmdMsg) {
    if (permissionFailed(PermissionCheck.MOD_LEVEL, PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId) || liveMeeting.props.meetingProp.isBreakout) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to mute all except presenters."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
    } else {
      if (msg.body.mute != MeetingStatus2x.isMeetingMuted(liveMeeting.status)) {
        if (msg.body.mute) {
          val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
            liveMeeting.props.meetingProp.intId,
            "info",
            "mute",
            "app.toast.meetingMuteOnViewers.label",
            "Message used when viewers of a meeting have been muted",
            Vector()
          )
          outGW.send(notifyEvent)
          NotificationDAO.insert(notifyEvent)

          MeetingStatus2x.muteMeeting(liveMeeting.status)
        } else {
          val notifyEvent = MsgBuilder.buildNotifyAllInMeetingEvtMsg(
            liveMeeting.props.meetingProp.intId,
            "info",
            "unmute",
            "app.toast.meetingMuteOff.label",
            "Message used when meeting has been unmuted",
            Vector()
          )
          outGW.send(notifyEvent)
          NotificationDAO.insert(notifyEvent)

          MeetingStatus2x.unmuteMeeting(liveMeeting.status)
        }

        val muted = MeetingStatus2x.isMeetingMuted(liveMeeting.status)
        val event = build(props.meetingProp.intId, msg.body.mutedBy, muted, msg.body.mutedBy)

        outGW.send(event)

        // We no longer want to unmute users when meeting mute is turned off
        if (muted) {
          // I think the correct flow would be to find those who are presenters and exclude them
          // from the list of voice users. The remaining, mute.
          VoiceUsers.findAll(liveMeeting.voiceUsers) foreach { vu =>
            if (!vu.listenOnly) {
              Users2x.findWithIntId(liveMeeting.users2x, vu.intId) match {
                case Some(u) => if (!u.presenter) muteUserInVoiceConf(vu, muted)
                case None    => muteUserInVoiceConf(vu, muted)
              }
            }
          }
        }
      }
    }
  }

  def usersWhoAreNotPresenter(): Vector[UserState] = {
    Users2x.findNotPresenters(liveMeeting.users2x)
  }

  def build(meetingId: String, userId: String, muted: Boolean, mutedBy: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
    val envelope = BbbCoreEnvelope(MeetingMutedEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(MeetingMutedEvtMsg.NAME, meetingId, userId)

    val body = MeetingMutedEvtMsgBody(muted, mutedBy)
    val event = MeetingMutedEvtMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def muteUserInVoiceConf(vu: VoiceUserState, mute: Boolean): Unit = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, props.meetingProp.intId, vu.intId)
    val envelope = BbbCoreEnvelope(MuteUserInVoiceConfSysMsg.NAME, routing)
    val header = BbbCoreHeaderWithMeetingId(MuteUserInVoiceConfSysMsg.NAME, props.meetingProp.intId)

    val body = MuteUserInVoiceConfSysMsgBody(props.voiceProp.voiceConf, vu.voiceUserId, mute)
    val event = MuteUserInVoiceConfSysMsg(header, body)
    val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

    outGW.send(msgEvent)

  }

}
