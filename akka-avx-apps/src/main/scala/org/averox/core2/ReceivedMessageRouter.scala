package org.averox.core2

import org.averox.SystemConfiguration
import org.averox.core.bus.{ BbbMsgEvent, BbbMsgRouterEventBus }

trait ReceivedMessageRouter extends SystemConfiguration {
  val eventBus: BbbMsgRouterEventBus

  def publish(msg: BbbMsgEvent): Unit = {
    eventBus.publish(msg)
  }

}
