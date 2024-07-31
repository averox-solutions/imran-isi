package org.averox.core.apps.users

import org.averox.ClientSettings.{ getConfigPropertyValueByPath, getConfigPropertyValueByPathAsIntOrElse }
import org.averox.common2.msgs._
import org.averox.core.apps.RightsManagementTrait
import org.averox.core.models.{ UserState, Users2x }
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }

trait ChangeUserReactionEmojiReqMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleChangeUserReactionEmojiReqMsg(msg: ChangeUserReactionEmojiReqMsg): Unit = {
    log.info("handleChangeUserReactionEmojiReqMsg: reactionEmoji={} userId={}", msg.body.reactionEmoji, msg.body.userId)

    def broadcast(user: UserState, reactionEmoji: String): Unit = {
      val routingChange = Routing.addMsgToClientRouting(
        MessageTypes.BROADCAST_TO_MEETING,
        liveMeeting.props.meetingProp.intId, user.intId
      )
      val envelopeChange = BbbCoreEnvelope(UserReactionEmojiChangedEvtMsg.NAME, routingChange)
      val headerChange = BbbClientMsgHeader(UserReactionEmojiChangedEvtMsg.NAME, liveMeeting.props.meetingProp.intId,
        user.intId)

      val bodyChange = UserReactionEmojiChangedEvtMsgBody(user.intId, reactionEmoji)
      val eventChange = UserReactionEmojiChangedEvtMsg(headerChange, bodyChange)
      val msgEventChange = BbbCommonEnvCoreMsg(envelopeChange, eventChange)
      outGW.send(msgEventChange)
    }

    //Get durationInSeconds from Client config
    val userReactionExpire = getConfigPropertyValueByPathAsIntOrElse(liveMeeting.clientSettings, "public.userReaction.expire", 30)
    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
      newUserState <- Users2x.setReactionEmoji(liveMeeting.users2x, user.intId, msg.body.reactionEmoji, userReactionExpire)
    } yield {
      if (user.reactionEmoji != msg.body.reactionEmoji) {
        broadcast(newUserState, msg.body.reactionEmoji)
      }
    }
  }
}