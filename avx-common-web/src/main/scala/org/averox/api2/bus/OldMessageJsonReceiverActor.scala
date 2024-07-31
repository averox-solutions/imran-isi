package org.averox.api2.bus

import org.apache.pekko.actor.{ Actor, ActorLogging, Props }
import org.averox.common2.bus.OldReceivedJsonMessage

object OldMessageJsonReceiverActor {
  def props(gw: OldMessageReceivedGW): Props = Props(classOf[OldMessageJsonReceiverActor], gw)
}

class OldMessageJsonReceiverActor(gw: OldMessageReceivedGW) extends Actor with ActorLogging {

  def receive = {
    case msg: OldReceivedJsonMessage => //gw.handle(msg.pattern, msg.channel, msg.msg)
  }
}
