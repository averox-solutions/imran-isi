package org.averox.core.apps.pads

import org.apache.pekko.actor.ActorContext

class PadsApp2x(implicit val context: ActorContext)
  extends PadGroupCreatedEvtMsgHdlr
  with PadCreateReqMsgHdlr
  with PadCreatedEvtMsgHdlr
  with PadCreateSessionReqMsgHdlr
  with PadSessionCreatedEvtMsgHdlr
  with PadSessionDeletedSysMsgHdlr
  with PadUpdatedSysMsgHdlr
  with PadContentSysMsgHdlr
  with PadPatchSysMsgHdlr
  with PadUpdatePubMsgHdlr
  with PadPinnedReqMsgHdlr {

}
