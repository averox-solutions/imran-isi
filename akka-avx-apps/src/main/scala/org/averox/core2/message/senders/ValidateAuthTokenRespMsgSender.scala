package org.averox.core2.message.senders

import org.averox.common2.msgs._
import org.averox.core.running.OutMsgRouter

object ValidateAuthTokenRespMsgSender {

  def send(outGW: OutMsgRouter, meetingId: String, userId: String, authToken: String,
           valid: Boolean, waitForApproval: Boolean, registeredOn: Long, authTokenValidatedOn: Long, reasonCode: String, reason: String): Unit = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(ValidateAuthTokenRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(ValidateAuthTokenRespMsg.NAME, meetingId, userId)
    val body = ValidateAuthTokenRespMsgBody(userId, authToken, valid, waitForApproval, registeredOn, authTokenValidatedOn, reasonCode, reason)
    val event = ValidateAuthTokenRespMsg(header, body)
    val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
    outGW.send(msgEvent)
  }
}