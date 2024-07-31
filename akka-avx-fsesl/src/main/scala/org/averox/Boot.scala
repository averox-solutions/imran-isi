package org.averox

import org.averox.common2.bus.IncomingJsonMessageBus
import org.averox.common2.redis.{ RedisConfig, RedisPublisher }
import org.averox.endpoint.redis.FSESLRedisSubscriberActor
import org.averox.freeswitch.{ RxJsonMsgHdlrActor, VoiceConferenceService }
import org.averox.freeswitch.voice.FreeswitchConferenceEventListener
import org.averox.freeswitch.voice.freeswitch.{ ConnectionManager, ESLEventListener, FreeswitchApplication }
import org.freeswitch.esl.client.manager.DefaultManagerConnection
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.ActorMaterializer
import org.apache.pekko.http.scaladsl.Http
import org.averox.service.HealthzService

import scala.concurrent.ExecutionContext

object Boot extends App with SystemConfiguration with WebApi {

  override implicit val system = ActorSystem("averox-fsesl-system")
  override implicit val executor: ExecutionContext = system.dispatcher
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  val redisPass = if (redisPassword != "") Some(redisPassword) else None
  val redisConfig = RedisConfig(redisHost, redisPort, redisPass, redisExpireKey)

  val redisPublisher = new RedisPublisher(
    system,
    "BbbFsEslAkkaPub",
    redisConfig
  )

  val eslConnection = new DefaultManagerConnection(eslHost, eslPort, eslPassword)

  val healthz = HealthzService(system)

  val voiceConfService = new VoiceConferenceService(healthz, redisPublisher)

  val fsConfEventListener = new FreeswitchConferenceEventListener(voiceConfService)
  fsConfEventListener.start()

  val eslEventListener = new ESLEventListener(fsConfEventListener)
  val connManager = new ConnectionManager(eslConnection, eslEventListener, fsConfEventListener)

  connManager.start()

  val fsApplication = new FreeswitchApplication(connManager, fsProfile)
  fsApplication.start()

  val inJsonMsgBus = new IncomingJsonMessageBus
  val redisMessageHandlerActor = system.actorOf(RxJsonMsgHdlrActor.props(fsApplication))
  inJsonMsgBus.subscribe(redisMessageHandlerActor, toFsAppsJsonChannel)

  val channelsToSubscribe = Seq(toVoiceConfRedisChannel)

  val redisSubscriberActor = system.actorOf(
    FSESLRedisSubscriberActor.props(
      system,
      inJsonMsgBus,
      redisConfig,
      channelsToSubscribe,
      Nil,
      toFsAppsJsonChannel
    ),
    "redis-subscriber"
  )

  val apiService = new ApiService(healthz)

  val bindingFuture = Http().bindAndHandle(apiService.routes, httpHost, httpPort)

}
