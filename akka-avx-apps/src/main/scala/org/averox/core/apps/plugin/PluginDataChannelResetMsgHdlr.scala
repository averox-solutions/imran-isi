package org.averox.core.apps.plugin

import org.averox.ClientSettings
import org.averox.common2.msgs.PluginDataChannelResetMsg
import org.averox.core.db.PluginDataChannelEntryDAO
import org.averox.core.domain.MeetingState2x
import org.averox.core.models.{ Roles, Users2x }
import org.averox.core.running.{ HandlerHelpers, LiveMeeting }

trait PluginDataChannelResetMsgHdlr extends HandlerHelpers {

  def handle(msg: PluginDataChannelResetMsg, state: MeetingState2x, liveMeeting: LiveMeeting): Unit = {
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
            case _           => false
          }
        }

        if (!hasPermission.contains(true)) {
          println(s"No permission to delete (reset) in plugin: '${msg.body.pluginName}', data channel: '${msg.body.channelName}'.")
        } else {
          PluginDataChannelEntryDAO.reset(
            meetingId,
            msg.body.pluginName,
            msg.body.channelName,
            msg.body.subChannelName
          )
        }
      }
    }
  }
}