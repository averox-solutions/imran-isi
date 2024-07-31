package org.averox.core.bus

import org.apache.pekko.actor.ActorRef
import org.averox.core.api.InMessage

case class AveroxEvent(val topic: String, val payload: InMessage)

trait InternalEventBus {

  def publish(event: AveroxEvent): Unit
  def subscribe(actorRef: ActorRef, topic: String)
  def unsubscribe(actorRef: ActorRef, topic: String)
}

