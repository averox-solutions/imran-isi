package org.averox.freeswitch

import org.averox.SystemConfiguration
import org.averox.common2.bus.ReceivedJsonMessage
import org.averox.common2.msgs._
import org.averox.freeswitch.voice.freeswitch.FreeswitchApplication

import com.fasterxml.jackson.databind.JsonNode

import org.apache.pekko.actor.Actor
import org.apache.pekko.actor.ActorLogging
import org.apache.pekko.actor.Props

object RxJsonMsgHdlrActor {
  def props(fsApp: FreeswitchApplication): Props =
    Props(classOf[RxJsonMsgHdlrActor], fsApp)
}

class RxJsonMsgHdlrActor(val fsApp: FreeswitchApplication) extends Actor with ActorLogging
  with SystemConfiguration with RxJsonMsgDeserializer {
  def receive = {
    case msg: ReceivedJsonMessage =>
      log.debug("handling {} - {}", msg.channel, msg.data)
      handleReceivedJsonMessage(msg)
    case _ => // do nothing
  }

  def handleReceivedJsonMessage(msg: ReceivedJsonMessage): Unit = {
    for {
      envJsonNode <- JsonDeserializer.toBbbCommonEnvJsNodeMsg(msg.data)
    } yield handle(envJsonNode.envelope, envJsonNode.core)
  }

  def handle(envelope: BbbCoreEnvelope, jsonNode: JsonNode): Unit = {
    //log.debug("Route envelope name " + envelope.name)
    envelope.name match {
      case GetUsersInVoiceConfSysMsg.NAME =>
        routeGetUsersInVoiceConfSysMsg(envelope, jsonNode)
      case EjectAllFromVoiceConfMsg.NAME =>
        routeEjectAllFromVoiceConfMsg(envelope, jsonNode)
      case EjectUserFromVoiceConfSysMsg.NAME =>
        routeEjectUserFromVoiceConfMsg(envelope, jsonNode)
      case MuteUserInVoiceConfSysMsg.NAME =>
        routeMuteUserInVoiceConfMsg(envelope, jsonNode)
      case DeafUserInVoiceConfSysMsg.NAME =>
        routeDeafUserInVoiceConfMsg(envelope, jsonNode)
      case HoldUserInVoiceConfSysMsg.NAME =>
        routeHoldUserInVoiceConfMsg(envelope, jsonNode)
      case PlaySoundInVoiceConfSysMsg.NAME =>
        routePlaySoundInVoiceConfMsg(envelope, jsonNode)
      case StopSoundInVoiceConfSysMsg.NAME =>
        routeStopSoundInVoiceConfMsg(envelope, jsonNode)
      case TransferUserToVoiceConfSysMsg.NAME =>
        routeTransferUserToVoiceConfMsg(envelope, jsonNode)
      case StartRecordingVoiceConfSysMsg.NAME =>
        routeStartRecordingVoiceConfMsg(envelope, jsonNode)
      case StopRecordingVoiceConfSysMsg.NAME =>
        routeStopRecordingVoiceConfMsg(envelope, jsonNode)
      case CheckRunningAndRecordingToVoiceConfSysMsg.NAME =>
        routeCheckRunningAndRecordingToVoiceConfSysMsg(envelope, jsonNode)
      case GetUsersStatusToVoiceConfSysMsg.NAME =>
        routeGetUsersStatusToVoiceConfSysMsg(envelope, jsonNode)
      case HoldChannelInVoiceConfSysMsg.NAME =>
        routeHoldChannelInVoiceConfMsg(envelope, jsonNode)
      case _ => // do nothing
    }
  }
}