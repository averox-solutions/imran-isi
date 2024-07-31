const e = require('../core/elements');

// Create Parameters
exports.bannerText = 'bannerText=some+text';
const color = '#FFFF00'
exports.color = color;
exports.bannerColor = `bannerColor=${color}`;
exports.maxParticipants = 'maxParticipants=2';
exports.duration = 'duration=2';
const messageModerator = 'Test';
exports.messageModerator = messageModerator;
exports.moderatorOnlyMessage = `moderatorOnlyMessage=${messageModerator}`;
exports.webcamsOnlyForModerator = 'webcamsOnlyForModerator=true';
exports.muteOnStart = 'muteOnStart=true';
exports.allowModsToUnmuteUsers = 'allowModsToUnmuteUsers=true';
exports.lockSettingsDisableCam = 'lockSettingsDisableCam=true';
exports.lockSettingsDisableMic = 'lockSettingsDisableMic=true';
exports.lockSettingsDisablePublicChat = 'lockSettingsDisablePublicChat=true';
exports.lockSettingsHideUserList = 'lockSettingsHideUserList=true';
exports.allowModsToEjectCameras = 'allowModsToEjectCameras=true';
exports.notifyRecordingIsOn = 'notifyRecordingIsOn=true&notifyRecordingIsOn=true';

// Custom Parameters
exports.autoJoin = 'userdata-avx_auto_join_audio=false';
exports.listenOnlyMode = 'userdata-avx_listen_only_mode=false';
exports.forceListenOnly = 'userdata-avx_force_listen_only=true';
exports.skipCheck = 'userdata-avx_skip_check_audio=true';
exports.skipCheckOnFirstJoin = 'userdata-avx_skip_check_audio_on_first_join=true';
exports.skipEchoTestIfPreviousDevice = 'userdata-avx_skip_echotest_if_previous_device=true';
const docTitle = 'playwright';
exports.docTitle = docTitle;
exports.clientTitle = `userdata-avx_client_title=${docTitle}`;
exports.askForFeedbackOnLogout = 'userdata-avx_ask_for_feedback_on_logout=true';
exports.displayBrandingArea = 'userdata-avx_display_branding_area=true';
exports.logo = 'logo=https://averox.org/wp-content/uploads/2021/01/Averox_icon.svg.png';
exports.enableVideo = 'userdata-avx_enable_video=false';
exports.autoShareWebcam = 'userdata-avx_auto_share_webcam=true';
exports.multiUserPenOnly = 'userdata-avx_multi_user_pen_only=true';
exports.presenterTools = 'userdata-avx_presenter_tools=["select","draw", "arrow"]';
exports.multiUserTools = 'userdata-avx_multi_user_tools=["arrow","text"]';
const cssCode = `${e.presentationTitle}{display: none;}`;
exports.customStyle = `userdata-avx_custom_style=${cssCode}`;
exports.customStyleUrl = 'userdata-avx_custom_style_url=https://develop.averox.org/css-test-file.css';
exports.autoSwapLayout = 'userdata-avx_auto_swap_layout=true';
exports.hidePresentationOnJoin = 'userdata-avx_hide_presentation_on_join=true';
exports.outsideToggleSelfVoice = 'userdata-avx_outside_toggle_self_voice=true';
exports.outsideToggleRecording = 'userdata-avx_outside_toggle_recording=true';
exports.showPublicChatOnLogin = 'userdata-avx_show_public_chat_on_login=false';
exports.forceRestorePresentationOnNewEvents = 'userdata-avx_force_restore_presentation_on_new_events=true';
exports.recordMeeting = 'record=true';
exports.skipVideoPreview = 'userdata-avx_skip_video_preview=true';
exports.skipVideoPreviewOnFirstJoin = 'userdata-avx_skip_video_preview_on_first_join=true';
exports.skipVideoPreviewIfPreviousDevice = 'userdata-avx_skip_video_preview_if_previous_device=true';
exports.mirrorOwnWebcam = 'userdata-avx_mirror_own_webcam=true';
exports.showParticipantsOnLogin = 'userdata-avx_show_participants_on_login=false';
exports.hideActionsBar = 'userdata-avx_hide_actions_bar=true';
exports.overrideDefaultLocale = 'userdata-avx_override_default_locale=pt-br';
exports.hideNavBar = 'userdata-avx_hide_nav_bar=true';
exports.preferredCameraProfile = 'userdata-avx_preferred_camera_profile=low';

// Disabled Features
exports.breakoutRoomsDisabled = 'disabledFeatures=breakoutRooms';
exports.speechRecognitionDisabled = 'disabledFeatures=liveTranscription';
exports.captionsDisabled = 'disabledFeatures=captions';
exports.chatDisabled = 'disabledFeatures=chat';
exports.externalVideosDisabled = 'disabledFeatures=externalVideos';
exports.learningDashboardDisabled = 'disabledFeatures=learningDashboard';
exports.pollsDisabled = 'disabledFeatures=polls';
exports.screenshareDisabled = 'disabledFeatures=screenshare';
exports.sharedNotesDisabled = 'disabledFeatures=sharedNotes';
exports.virtualBackgroundsDisabled = 'disabledFeatures=virtualBackgrounds';
exports.downloadPresentationWithAnnotationsDisabled = 'disabledFeatures=downloadPresentationWithAnnotations';
exports.importPresentationWithAnnotationsFromBreakoutRoomsDisabled = 'disabledFeatures=importPresentationWithAnnotationsFromBreakoutRooms';
exports.importSharedNotesFromBreakoutRoomsDisabled = 'disabledFeatures=importSharedNotesFromBreakoutRooms';
exports.layoutsDisabled = 'disabledFeatures=layouts';
exports.presentationDisabled = 'disabledFeatures=presentation';
exports.customVirtualBackgroundDisabled = 'disabledFeatures=customVirtualBackgrounds';
exports.slideSnapshotDisabled = 'disabledFeatures=snapshotOfCurrentSlide';
exports.cameraAsContent = 'disabledFeatures=cameraAsContent';

// Disabled Features Exclude
exports.breakoutRoomsExclude = 'disabledFeatures=breakoutRooms,presentation,chat&disabledFeaturesExclude=breakoutRooms';
exports.speechRecognitionExclude = 'disabledFeatures=breakoutRooms,presentation,chat,liveTranscription&disabledFeaturesExclude=liveTranscription';
exports.captionsExclude = 'disabledFeatures=captions,presentation,chat&disabledFeaturesExclude=captions';
exports.chatExclude = 'disabledFeatures=presentation,chat&disabledFeaturesExclude=chat';
exports.externalVideosExclude = 'disabledFeatures=presentation,chat,externalVideos&disabledFeaturesExclude=externalVideos';
exports.layoutsExclude = 'disabledFeatures=presentation,chat,layouts&disabledFeaturesExclude=layouts';
exports.learningDashboardExclude = 'disabledFeatures=presentation,chat,learningDashboard&disabledFeaturesExclude=learningDashboard';
exports.pollsExclude = 'disabledFeatures=layouts,polls&disabledFeaturesExclude=polls';
exports.screenshareExclude = 'disabledFeatures=presentation,chat,screenshare&disabledFeaturesExclude=screenshare';
exports.sharedNotesExclude = 'disabledFeatures=presentation,chat,sharedNotes&disabledFeaturesExclude=sharedNotes';
exports.virtualBackgroundsExclude = 'disabledFeatures=presentation,chat,virtualBackgrounds&disabledFeaturesExclude=virtualBackgrounds';
exports.downloadPresentationWithAnnotationsExclude = 'disabledFeatures=chat,downloadPresentationWithAnnotations&disabledFeaturesExclude=downloadPresentationWithAnnotations';
exports.importPresentationWithAnnotationsFromBreakoutRoomsExclude = 'disabledFeatures=presentation,chat,importPresentationWithAnnotationsFromBreakoutRooms&disabledFeaturesExclude=importPresentationWithAnnotationsFromBreakoutRooms';
exports.importSharedNotesFromBreakoutRoomsExclude = 'disabledFeatures=presentation,chat,importSharedNotesFromBreakoutRooms&disabledFeaturesExclude=importSharedNotesFromBreakoutRooms';
exports.presentationExclude = 'disabledFeatures=presentation,chat&disabledFeaturesExclude=presentation';
exports.customVirtualBackgroundExclude = 'disabledFeatures=presentation,chat,customVirtualBackground&disabledFeaturesExclude=customVirtualBackground';
exports.slideSnapshotExclude = 'disabledFeatures=snapShotOfCurrentSlide,chat&disabledFeaturesExclude=snapShotOfCurrentSlide';
exports.cameraAsContentExclude = 'disabledFeatures=cameraAsContent,chat&disabledFeaturesExclude=cameraAsContent';

// Shortcuts
exports.shortcuts = 'userdata-avx_shortcuts=[$]';
exports.initialShortcuts = [{
  param: 'openOptions',
  key: 'O'
}, {
  param: 'toggleUserList',
  key: 'U'
}, {
  param: 'togglePublicChat',
  key: 'P'
}, {
  param: 'openActions',
  key: 'A'
}, {
  param: 'joinAudio',
  key: 'J'
}];
exports.laterShortcuts = [{
  param: 'toggleMute',
  key: 'M'
}, {
  param: 'leaveAudio',
  key: 'L'
}, {
  param: 'hidePrivateChat',
  key: 'H'
}, {
  param: 'closePrivateChat',
  key: 'G'
}];
