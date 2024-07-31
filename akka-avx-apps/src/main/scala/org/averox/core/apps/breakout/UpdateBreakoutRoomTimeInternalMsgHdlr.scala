package org.averox.core.apps.breakout

import org.averox.core.api.{ UpdateBreakoutRoomTimeInternalMsg }
import org.averox.core.domain.{ MeetingState2x }
import org.averox.core.running.{ MeetingActor, OutMsgRouter }

trait UpdateBreakoutRoomTimeInternalMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMsgRouter

  def handleUpdateBreakoutRoomTimeInternalMsgHdlr(msg: UpdateBreakoutRoomTimeInternalMsg, state: MeetingState2x): MeetingState2x = {

    val breakoutModel = for {
      model <- state.breakout
    } yield {
      val updatedBreakoutModel = model.setTime(msg.durationInSeconds)
      updatedBreakoutModel
    }

    breakoutModel match {
      case Some(model) => state.update(Some(model))
      case None        => state
    }

  }
}
