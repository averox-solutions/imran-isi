package org.averox.core.bus

import org.apache.pekko.actor.ActorRef

class InMsgBusGW(bus: IncomingEventBusImp) extends InternalEventBus {
  override def publish(event: AveroxEvent): Unit = {
    bus.publish(event)
  }

  override def subscribe(actorRef: ActorRef, topic: String): Unit = {
    bus.subscribe(actorRef, topic)
  }

  override def unsubscribe(actorRef: ActorRef, topic: String): Unit = {
    bus.unsubscribe(actorRef, topic)
  }
}
