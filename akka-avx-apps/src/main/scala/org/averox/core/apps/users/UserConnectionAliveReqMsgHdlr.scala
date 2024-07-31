package org.averox.core.apps.users

import org.averox.ClientSettings.getConfigPropertyValueByPathAsIntOrElse
import org.averox.common2.msgs._
import org.averox.core.apps.RightsManagementTrait
import org.averox.core.db.UserConnectionStatusDAO
import org.averox.core.models.Users2x
import org.averox.core.running.{ LiveMeeting, OutMsgRouter }

trait UserConnectionAliveReqMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

  def handleUserConnectionAliveReqMsg(msg: UserConnectionAliveReqMsg): Unit = {
    log.info("handleUserConnectionAliveReqMsg: networkRttInMs={} userId={}", msg.body.networkRttInMs, msg.body.userId)

    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.body.userId)
    } yield {
      val rtt: Option[Double] = msg.body.networkRttInMs match {
        case 0           => None
        case rtt: Double => Some(rtt)
      }

      val status = getLevelFromRtt(msg.body.networkRttInMs)

      UserConnectionStatusDAO.updateUserAlive(user.meetingId, user.intId, rtt, status)
    }
  }

  def getLevelFromRtt(networkRttInMs: Double): String = {
    val clientSettings = liveMeeting.clientSettings
    val warningValue = getConfigPropertyValueByPathAsIntOrElse(clientSettings, "public.stats.rtt.warning", 500)
    val dangerValue = getConfigPropertyValueByPathAsIntOrElse(clientSettings, "public.stats.rtt.danger", 1000)
    val criticalValue = getConfigPropertyValueByPathAsIntOrElse(clientSettings, "public.stats.rtt.critical", 2000)

    networkRttInMs match {
      case rtt if rtt > criticalValue => "critical"
      case rtt if rtt > dangerValue   => "danger"
      case rtt if rtt > warningValue  => "warning"
      case _                          => "normal"
    }
  }

}
