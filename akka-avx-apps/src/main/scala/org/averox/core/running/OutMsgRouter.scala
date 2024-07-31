package org.averox.core.running

import org.averox.common2.msgs.{ BbbCommonEnvCoreMsg }
import org.averox.core.OutMessageGateway

class OutMsgRouter(record: Boolean, val outGW: OutMessageGateway) {
  def send(msg: BbbCommonEnvCoreMsg): Unit = {
    outGW.send(msg)
    if (record) outGW.record(msg)
  }
}
