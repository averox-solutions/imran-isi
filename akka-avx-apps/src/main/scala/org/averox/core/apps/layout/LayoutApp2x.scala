package org.averox.core.apps.layout

import org.averox.core.models.{ Layouts, Roles, Users2x }
import org.averox.core.running.MeetingActor

trait LayoutApp2x
  extends BroadcastLayoutMsgHdlr
  with BroadcastPushLayoutMsgHdlr
  with GetCurrentLayoutReqMsgHdlr {

  this: MeetingActor =>
}
