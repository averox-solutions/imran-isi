package org.averox.api2.bus

import org.averox.api.IReceivedOldMessageHandler
import org.averox.api.messaging.messages.IMessage

class OldMessageReceivedGW(handler: IReceivedOldMessageHandler) {

  def handle(msg: IMessage): Unit = {
    handler.handleMessage(msg)
  }

}
