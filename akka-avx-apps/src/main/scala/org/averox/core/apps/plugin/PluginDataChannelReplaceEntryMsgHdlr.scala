package org.averox.core.apps.plugin

import org.averox.ClientSettings
import org.averox.common2.msgs.PluginDataChannelReplaceEntryMsg
import org.averox.core.db.{JsonUtils, PluginDataChannelEntryDAO}
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.{Roles, Users2x}
import org.averox.core.running.{HandlerHelpers, LiveMeeting}

trait PluginDataChannelReplaceEntryMsgHdlr extends HandlerHelpers {

  def handle(msg: PluginDataChannelReplaceEntryMsg, state: MeetingState2x, liveMeeting: LiveMeeting): Unit = {
    val pluginsDisabled: Boolean = liveMeeting.props.meetingProp.disabledFeatures.contains("plugins")
    val meetingId = liveMeeting.props.meetingProp.intId

    for {
      _ <- if (!pluginsDisabled) Some(()) else None
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      val pluginsConfig = ClientSettings.getPluginsFromConfig(ClientSettings.clientSettingsFromFile)

      if (!pluginsConfig.contains(msg.body.pluginName)) {
        println(s"Plugin '${msg.body.pluginName}' not found.")
      } else if (!pluginsConfig(msg.body.pluginName).dataChannels.contains(msg.body.channelName)) {
        println(s"Data channel '${msg.body.channelName}' not found in plugin '${msg.body.pluginName}'.")
      } else {
        val hasPermission = for {
          replaceOrDeletePermission <- pluginsConfig(msg.body.pluginName).dataChannels(msg.body.channelName).replaceOrDeletePermission
        } yield {
          replaceOrDeletePermission.toLowerCase match {
            case "all"       => true
            case "moderator" => user.role == Roles.MODERATOR_ROLE
            case "presenter" => user.presenter
            case "creator" => {
              val creatorUserId = PluginDataChannelEntryDAO.getEntryCreator(
                meetingId,
                msg.body.pluginName,
                msg.body.channelName,
                msg.body.subChannelName,
                msg.body.entryId
              )
              creatorUserId == msg.header.userId
            }
            case _ => false
          }
        }

        if (!hasPermission.contains(true)) {
          println(s"No permission to write in plugin: '${msg.body.pluginName}', data channel: '${msg.body.channelName}'.")
        } else {
          PluginDataChannelEntryDAO.replace(
            msg.header.meetingId,
            msg.body.pluginName,
            msg.body.channelName,
            msg.body.subChannelName,
            msg.body.entryId,
            JsonUtils.mapToJson(msg.body.payloadJson),
          )
        }
      }
    }
  }
}
