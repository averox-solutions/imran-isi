package org.averox.core.running

import org.averox.common2.domain.DefaultProps
import org.averox.core.apps._
import org.averox.core.models._
import org.averox.core2.MeetingStatus2x

import java.util

class LiveMeeting(
    val props:               DefaultProps,
    val status:              MeetingStatus2x,
    val screenshareModel:    ScreenshareModel,
    val audioCaptions:       AudioCaptions,
    val timerModel:          TimerModel,
    val chatModel:           ChatModel,
    val externalVideoModel:  ExternalVideoModel,
    val layouts:             Layouts,
    val pads:                Pads,
    val registeredUsers:     RegisteredUsers,
    val polls:               Polls, // 2x
    val wbModel:             WhiteboardModel,
    val presModel:           PresentationModel,
    val captionModel:        CaptionModel,
    val webcams:             Webcams,
    val voiceUsers:          VoiceUsers,
    val users2x:             Users2x,
    val guestsWaiting:       GuestsWaiting,
    val clientSettings:      Map[String, Object],
)
