package org.averox.core

import java.io.{ PrintWriter, StringWriter }
import org.apache.pekko.actor._
import org.apache.pekko.actor.ActorLogging
import org.apache.pekko.actor.SupervisorStrategy.Resume
import org.apache.pekko.util.Timeout

import scala.concurrent.duration._
import org.averox.core.bus._
import org.averox.core.api._
import org.averox.SystemConfiguration

import java.util.concurrent.TimeUnit
import org.averox.common2.msgs._
import org.averox.core.db.{ DatabaseConnection, MeetingDAO }
import org.averox.core.domain.MeetingEndReason
import org.averox.core.models.Roles
import org.averox.core.running.RunningMeeting
import org.averox.core.util.ColorPicker
import org.averox.core2.RunningMeetings
import org.averox.core2.message.senders.MsgBuilder
import org.averox.service.HealthzService

object AveroxActor extends SystemConfiguration {
  def props(
      system:         ActorSystem,
      eventBus:       InternalEventBus,
      avxMsgBus:      BbbMsgRouterEventBus,
      outGW:          OutMessageGateway,
      healthzService: HealthzService
  ): Props =
    Props(classOf[AveroxActor], system, eventBus, avxMsgBus, outGW, healthzService)
}

class AveroxActor(
    val system:   ActorSystem,
    val eventBus: InternalEventBus, val avxMsgBus: BbbMsgRouterEventBus,
    val outGW:          OutMessageGateway,
    val healthzService: HealthzService
) extends Actor
  with ActorLogging with SystemConfiguration {

  implicit def executionContext = system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  private val meetings = new RunningMeetings

  private var sessionTokens = new collection.immutable.HashMap[String, (String, String)] //sessionToken -> (meetingId, userId)

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case e: Exception => {
      val sw: StringWriter = new StringWriter()
      sw.write("An exception has been thrown on AveroxActor, exception message [" + e.getMessage() + "] (full stacktrace below)\n")
      e.printStackTrace(new PrintWriter(sw))
      log.error(sw.toString())
      Resume
    }
  }

  override def preStart() {
    avxMsgBus.subscribe(self, meetingManagerChannel)
    DatabaseConnection.initialize()

    //Terminate all previous meetings, as they will not function following the akka-apps restart
    MeetingDAO.setAllMeetingsEnded(MeetingEndReason.ENDED_DUE_TO_SERVICE_INTERRUPTION, "system")
  }

  override def postStop() {
    avxMsgBus.unsubscribe(self, meetingManagerChannel)
  }

  def receive = {
    // Internal messages
    case msg: DestroyMeetingInternalMsg => handleDestroyMeeting(msg)

    //Api messages
    case msg: GetUserApiMsg             => handleGetUserApiMsg(msg, sender)

    // 2x messages
    case msg: BbbCommonEnvCoreMsg       => handleBbbCommonEnvCoreMsg(msg)
    case _                              => // do nothing
  }

  private def handleGetUserApiMsg(msg: GetUserApiMsg, actorRef: ActorRef): Unit = {
    log.debug("RECEIVED GetUserApiMsg msg {}", msg)

    sessionTokens.get(msg.sessionToken) match {
      case Some(sessionTokenInfo) =>
        RunningMeetings.findWithId(meetings, sessionTokenInfo._1) match {
          case Some(m) =>
            m.actorRef forward (msg)

          case None =>
            //The meeting is ended, it will return some data just to confirm the session was valid
            //The client can request data after the meeting is ended
            val userInfos = Map(
              "returncode" -> "SUCCESS",
              "sessionToken" -> msg.sessionToken,
              "meetingID" -> sessionTokenInfo._1,
              "internalUserID" -> sessionTokenInfo._2,
              "externMeetingID" -> "",
              "externUserID" -> "",
              "online" -> false,
              "authToken" -> "",
              "role" -> Roles.VIEWER_ROLE,
              "guest" -> "false",
              "guestStatus" -> "ALLOWED",
              "moderator" -> false,
              "presenter" -> false,
              "hideViewersCursor" -> false,
              "hideViewersAnnotation" -> false,
              "hideUserList" -> false,
              "webcamsOnlyForModerator" -> false
            )
            actorRef ! ApiResponseSuccess("Meeting is ended!", UserInfosApiMsg(userInfos))
        }
      case None =>
        actorRef ! ApiResponseFailure("Meeting not found!")
    }
  }

  private def handleBbbCommonEnvCoreMsg(msg: BbbCommonEnvCoreMsg): Unit = {
    msg.core match {

      case m: CreateMeetingReqMsg                    => handleCreateMeetingReqMsg(m)
      case m: RegisterUserReqMsg                     => handleRegisterUserReqMsg(m)
      case m: GetAllMeetingsReqMsg                   => handleGetAllMeetingsReqMsg(m)
      case m: GetRunningMeetingsReqMsg               => handleGetRunningMeetingsReqMsg(m)
      case m: CheckAlivePingSysMsg                   => handleCheckAlivePingSysMsg(m)
      case m: ValidateConnAuthTokenSysMsg            => handleValidateConnAuthTokenSysMsg(m)
      case _: UserGraphqlConnectionEstablishedSysMsg => //Ignore
      case _: UserGraphqlConnectionClosedSysMsg      => //Ignore
      case _: CheckGraphqlMiddlewareAlivePongSysMsg  => //Ignore
      case _                                         => log.warning("Cannot handle " + msg.envelope.name)
    }
  }

  def handleValidateConnAuthTokenSysMsg(msg: ValidateConnAuthTokenSysMsg): Unit = {
    RunningMeetings.findWithId(meetings, msg.body.meetingId) match {
      case Some(meeting) =>
        meeting.actorRef forward msg

      case None =>
        val event = MsgBuilder.buildValidateConnAuthTokenSysRespMsg(msg.body.meetingId, msg.body.userId,
          false, msg.body.connId, msg.body.app)
        outGW.send(event)
    }
  }

  def handleRegisterUserReqMsg(msg: RegisterUserReqMsg): Unit = {
    log.debug("RECEIVED RegisterUserReqMsg msg {}", msg)
    for {
      m <- RunningMeetings.findWithId(meetings, msg.header.meetingId)
    } yield {
      log.debug("FORWARDING Register user message")

      //Store sessionTokens and associate them with their respective meetingId + userId owners
      sessionTokens += (msg.body.sessionToken -> (msg.body.meetingId, msg.body.intUserId))

      m.actorRef forward (msg)
    }
  }

  def handleCreateMeetingReqMsg(msg: CreateMeetingReqMsg): Unit = {
    log.debug("RECEIVED CreateMeetingReqMsg msg {}", msg)

    RunningMeetings.findWithId(meetings, msg.body.props.meetingProp.intId) match {
      case None =>
        log.info("Create meeting request. meetingId={}", msg.body.props.meetingProp.intId)

        val m = RunningMeeting(msg.body.props, outGW, eventBus)

        // Subscribe to meeting and voice events.
        eventBus.subscribe(m.actorRef, m.props.meetingProp.intId)
        eventBus.subscribe(m.actorRef, m.props.voiceProp.voiceConf)

        avxMsgBus.subscribe(m.actorRef, m.props.meetingProp.intId)
        avxMsgBus.subscribe(m.actorRef, m.props.voiceProp.voiceConf)

        RunningMeetings.add(meetings, m)

      case Some(m) =>
        log.info("Meeting already created. meetingID={}", msg.body.props.meetingProp.intId)
      // do nothing

    }
  }

  private def handleGetRunningMeetingsReqMsg(msg: GetRunningMeetingsReqMsg): Unit = {
    val liveMeetings = RunningMeetings.meetings(meetings)
    val meetingIds = liveMeetings.map(m => m.props.meetingProp.intId)

    val routing = collection.immutable.HashMap("sender" -> "avx-apps-akka")
    val envelope = BbbCoreEnvelope(GetRunningMeetingsRespMsg.NAME, routing)
    val header = BbbCoreBaseHeader(GetRunningMeetingsRespMsg.NAME)

    val body = GetRunningMeetingsRespMsgBody(meetingIds)
    val event = GetRunningMeetingsRespMsg(header, body)
    val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
    outGW.send(msgEvent)
  }

  private def handleGetAllMeetingsReqMsg(msg: GetAllMeetingsReqMsg): Unit = {
    RunningMeetings.meetings(meetings).foreach(m => {
      m.actorRef ! msg
    })
  }

  private def handleCheckAlivePingSysMsg(msg: CheckAlivePingSysMsg): Unit = {
    val event = MsgBuilder.buildCheckAlivePingSysMsg(msg.body.system, msg.body.avxWebTimestamp, System.currentTimeMillis())
    healthzService.sendPubSubStatusMessage(msg.body.akkaAppsTimestamp, System.currentTimeMillis())
    outGW.send(event)
  }

  private def handleDestroyMeeting(msg: DestroyMeetingInternalMsg): Unit = {

    for {
      m <- RunningMeetings.findWithId(meetings, msg.meetingId)
      m2 <- RunningMeetings.remove(meetings, msg.meetingId)
    } yield {
      // Unsubscribe to meeting and voice events.
      eventBus.unsubscribe(m.actorRef, m.props.meetingProp.intId)
      eventBus.unsubscribe(m.actorRef, m.props.voiceProp.voiceConf)

      avxMsgBus.unsubscribe(m.actorRef, m.props.meetingProp.intId)
      avxMsgBus.unsubscribe(m.actorRef, m.props.voiceProp.voiceConf)

      // Delay sending DisconnectAllUsers to allow messages to reach the client
      // before the connections are closed.
      context.system.scheduler.scheduleOnce(Duration.create(2500, TimeUnit.MILLISECONDS)) {
        // Disconnect all clients

        val disconnectEvnt = MsgBuilder.buildDisconnectAllClientsSysMsg(msg.meetingId, "meeting-destroyed")
        m2.outMsgRouter.send(disconnectEvnt)

        log.info("Destroyed meetingId={}", msg.meetingId)
        val destroyedEvent = MsgBuilder.buildMeetingDestroyedEvtMsg(msg.meetingId)
        m2.outMsgRouter.send(destroyedEvent)

        // Stop the meeting actor.
        context.stop(m.actorRef)
      }

      //Delay removal of session tokens and Graphql data once users might request some info after the meeting is ended
      context.system.scheduler.scheduleOnce(Duration.create(60, TimeUnit.MINUTES)) {
        log.debug("Removing Graphql data and session tokens. meetingID={}", msg.meetingId)

        sessionTokens = sessionTokens.filter(sessionTokenInfo => sessionTokenInfo._2._1 != msg.meetingId)

        //In Db, Removing the meeting is enough, all other tables has "ON DELETE CASCADE"
        MeetingDAO.delete(msg.meetingId)
      }

      //Remove ColorPicker idx of the meeting
      ColorPicker.reset(m.props.meetingProp.intId)
    }
  }

}