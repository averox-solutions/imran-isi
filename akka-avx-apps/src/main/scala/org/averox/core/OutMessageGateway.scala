package org.averox.core

import org.averox.common2.msgs.{ BbbCommonEnvCoreMsg }

trait OutMessageGateway {

  def send(msg: BbbCommonEnvCoreMsg): Unit

  def record(msg: BbbCommonEnvCoreMsg): Unit
}
