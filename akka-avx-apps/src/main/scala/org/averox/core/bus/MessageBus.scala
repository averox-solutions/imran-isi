package org.averox.core.bus

import org.averox.core.running.OutMsgRouter

case class MessageBus(eventBus: InternalEventBus, outGW: OutMsgRouter)

