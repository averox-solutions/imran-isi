package org.averox.core.apps.presentationpod

import org.averox.core.bus.MessageBus
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.PresentationPod
import org.averox.core2.message.senders.MsgBuilder
import org.averox.core.running.LiveMeeting
import org.averox.core.models.SystemUser

trait CreateDefaultPresentationPod {
  this: PresentationPodHdlrs =>

  def handleCreateDefaultPresentationPod(state: MeetingState2x, liveMeeting: LiveMeeting, bus: MessageBus): MeetingState2x = {
    val SYSTEM_ID = SystemUser.ID
    val resultPod: PresentationPod = PresentationPodsApp.createDefaultPresentationPod()
    val respMsg = MsgBuilder.buildCreateNewPresentationPodEvtMsg(
      liveMeeting.props.meetingProp.intId,
      resultPod.currentPresenter,
      resultPod.id,
      SYSTEM_ID
    )

    bus.outGW.send(respMsg)
    val pods = state.presentationPodManager.addPod(resultPod)
    state.update(pods)
  }
}
