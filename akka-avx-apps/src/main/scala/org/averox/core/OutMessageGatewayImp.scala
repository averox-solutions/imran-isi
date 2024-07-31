package org.averox.core

import org.averox.SystemConfiguration
import org.averox.common2.msgs.BbbCommonEnvCoreMsg
import org.averox.core.bus._

object OutMessageGatewayImp {
  def apply(
      outBus2: OutEventBus2
  ) =
    new OutMessageGatewayImp(outBus2)
}

class OutMessageGatewayImp(
    outBus2: OutEventBus2
) extends OutMessageGateway
  with SystemConfiguration {

  def send(msg: BbbCommonEnvCoreMsg): Unit = {
    outBus2.publish(BbbOutMessage(outBbbMsgMsgChannel, msg))
  }

  def record(msg: BbbCommonEnvCoreMsg): Unit = {
    outBus2.publish(BbbOutMessage(recordServiceMessageChannel, msg))
  }
}
