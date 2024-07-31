package org.averox.core

import akka.actor.ActorSystem
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import org.averox.SystemConfiguration
import org.averox.common2.domain.DefaultProps
import org.averox.common2.msgs._
import org.averox.core.bus._
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.duration._

class AveroxActorTestsSpec extends TestKit(ActorSystem(
  "AveroxActorTestsSpec",
  ConfigFactory.parseString(TestKitUsageSpec.config)
))
    with DefaultTimeout with ImplicitSender with WordSpecLike
    with Matchers with StopSystemAfterAll with AppsTestFixtures with SystemConfiguration {

  // See: http://doc.akka.io/docs/akka/current/scala/testing.html

  // Setup dependencies
  val avxMsgBus = new BbbMsgRouterEventBus
  val eventBus = new InMsgBusGW(new IncomingEventBusImp())
  val outBus2 = new OutEventBus2
  val recordBus = new RecordingEventBus

  //val outGW = OutMessageGatewayImp(outgoingEventBus, outBus2, recordBus)

  // Have the build in testActor receive messages coming from class under test (AveroxActor)
  outBus2.subscribe(testActor, outBbbMsgMsgChannel)

  "A AveroxActor" should {
    "Send a MeetingCreatedEvtMsg when receiving CreateMeetingReqMsg" in {
      within(500 millis) {

        val outGWSeq = new OutMsgGWSeq()
        // Create Averox Actor
        val avxActorRef = system.actorOf(AveroxActor.props(
          system,
          eventBus, avxMsgBus, outGWSeq
        ))

        // Send our create meeting request message
        val msg = buildCreateMeetingReqMsg(defaultProps)
        avxActorRef ! msg

        //assert(outGWSeq.msgs.length == 2)

        // Expect a message from AveroxActor as a result of handling
        // the create meeting request message.
        //expectMsgClass(classOf[BbbCommonEnvCoreMsg])
        //     expectMsgPF() {
        //       case event: BbbCommonEnvCoreMsg =>
        //         assert(event.envelope.name == MeetingCreatedEvtMsg.NAME)
        // Can do more assertions here
        //     }
      }
    }
  }

  def buildCreateMeetingReqMsg(props: DefaultProps): BbbCommonEnvCoreMsg = {
    val routing = collection.immutable.HashMap("sender" -> "avx-web")
    val envelope = BbbCoreEnvelope(CreateMeetingReqMsg.NAME, routing)
    val header = BbbCoreBaseHeader(CreateMeetingReqMsg.NAME)
    val body = CreateMeetingReqMsgBody(props)
    val req = CreateMeetingReqMsg(header, body)
    BbbCommonEnvCoreMsg(envelope, req)
  }
}