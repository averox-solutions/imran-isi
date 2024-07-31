package org.averox

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.event.Logging
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.stream.ActorMaterializer
import org.averox.common2.redis.{MessageSender, RedisConfig, RedisPublisher}
import org.averox.core._
import org.averox.core.bus._
import org.averox.core.pubsub.senders.ReceivedJsonMsgHandlerActor
import org.averox.core2.AnalyticsActor
import org.averox.core2.FromAkkaAppsMsgSenderActor
import org.averox.endpoint.redis.{AppsRedisSubscriberActor, ExportAnnotationsActor, GraphqlConnectionsActor, LearningDashboardActor, RedisRecorderActor}
import org.averox.common2.bus.IncomingJsonMessageBus
import org.averox.service.{HealthzService, MeetingInfoActor, MeetingInfoService, UserInfoService}

object Boot extends App with SystemConfiguration {

  implicit val system = ActorSystem("averox-apps-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  val logger = Logging(system, getClass)

  val eventBus = new InMsgBusGW(new IncomingEventBusImp())

  val outBus2 = new OutEventBus2
  val recordingEventBus = new RecordingEventBus

  val outGW = new OutMessageGatewayImp(outBus2)

  val redisPass = if (redisPassword != "") Some(redisPassword) else None
  val redisConfig = RedisConfig(redisHost, redisPort, redisPass, redisExpireKey)

  val redisPublisher = new RedisPublisher(
    system,
    "BbbAppsAkkaPub",
    redisConfig
  )

  val msgSender = new MessageSender(redisPublisher)
  val avxMsgBus = new BbbMsgRouterEventBus

  val healthzService = HealthzService(system)

  val meetingInfoActorRef = system.actorOf(MeetingInfoActor.props())

  outBus2.subscribe(meetingInfoActorRef, outBbbMsgMsgChannel)
  avxMsgBus.subscribe(meetingInfoActorRef, analyticsChannel)

  val meetingInfoService = MeetingInfoService(system, meetingInfoActorRef)

  val redisRecorderActor = system.actorOf(
    RedisRecorderActor.props(system, redisConfig, healthzService),
    "redisRecorderActor"
  )

  val exportAnnotationsActor = system.actorOf(
    ExportAnnotationsActor.props(system, redisConfig, healthzService),
    "exportAnnotationsActor"
  )

  val learningDashboardActor = system.actorOf(
    LearningDashboardActor.props(system, outGW),
    "LearningDashboardActor"
  )

  val graphqlConnectionsActor = system.actorOf(
    GraphqlConnectionsActor.props(system, eventBus, outGW),
    "GraphqlConnectionsActor"
  )

  ClientSettings.loadClientSettingsFromFile()
  recordingEventBus.subscribe(redisRecorderActor, outMessageChannel)
  val incomingJsonMessageBus = new IncomingJsonMessageBus

  val fromAkkaAppsMsgSenderActorRef = system.actorOf(FromAkkaAppsMsgSenderActor.props(msgSender))

  val analyticsActorRef = system.actorOf(AnalyticsActor.props(analyticsIncludeChat))
  outBus2.subscribe(fromAkkaAppsMsgSenderActorRef, outBbbMsgMsgChannel)
  outBus2.subscribe(redisRecorderActor, recordServiceMessageChannel)
  outBus2.subscribe(exportAnnotationsActor, outBbbMsgMsgChannel)

  outBus2.subscribe(analyticsActorRef, outBbbMsgMsgChannel)
  avxMsgBus.subscribe(analyticsActorRef, analyticsChannel)

  outBus2.subscribe(learningDashboardActor, outBbbMsgMsgChannel)
  avxMsgBus.subscribe(learningDashboardActor, analyticsChannel)

  eventBus.subscribe(graphqlConnectionsActor, meetingManagerChannel)
  avxMsgBus.subscribe(graphqlConnectionsActor, analyticsChannel)

  val avxActor = system.actorOf(AveroxActor.props(system, eventBus, avxMsgBus, outGW, healthzService), "averox-actor")
  eventBus.subscribe(avxActor, meetingManagerChannel)

  val userInfoService = UserInfoService(system, avxActor)
  val apiService = new ApiService(healthzService, meetingInfoService, userInfoService)

  val redisMessageHandlerActor = system.actorOf(ReceivedJsonMsgHandlerActor.props(avxMsgBus, incomingJsonMessageBus))
  incomingJsonMessageBus.subscribe(redisMessageHandlerActor, toAkkaAppsJsonChannel)

  val channelsToSubscribe = Seq(
    toAkkaAppsRedisChannel, fromVoiceConfRedisChannel, fromSfuRedisChannel,
  )

  val redisSubscriberActor = system.actorOf(
    AppsRedisSubscriberActor.props(
      system,
      incomingJsonMessageBus,
      redisConfig,
      channelsToSubscribe,
      Nil,
      toAkkaAppsJsonChannel
    ),
    "redis-subscriber"
  )

  val bindingFuture = Http().bindAndHandle(apiService.routes, httpHost, httpPort)
}
