exports.autoJoin = 'userdata-avx_auto_join_audio=false';
exports.listenOnlyMode = 'userdata-avx_listen_only_mode=false';
exports.forceListenOnly = 'userdata-avx_force_listen_only=true';
exports.skipCheck = 'userdata-avx_skip_check_audio=true';
exports.skipCheckOnFirstJoin = 'userdata-avx_skip_check_audio_on_first_join=true';
exports.skipEchoTestIfPreviousDevice = 'userdata-avx_skip_echotest_if_previous_device=true';
exports.docTitle = 'puppeteer';
exports.clientTitle = `userdata-avx_client_title=${this.docTitle}`;
exports.askForFeedbackOnLogout = 'userdata-avx_ask_for_feedback_on_logout=true';
exports.displayBrandingArea = 'userdata-avx_display_branding_area=true';
exports.logo = 'logo=https://averox.org/wp-content/themes/averox/library/images/averox-logo.png';
exports.enableScreensharing = 'userdata-avx_enable_screen_sharing=false';
exports.enableVideo = 'userdata-avx_enable_video=false';
exports.autoShareWebcam = 'userdata-avx_auto_share_webcam=true';
exports.multiUserPenOnly = 'userdata-avx_multi_user_pen_only=true';
exports.presenterTools = 'userdata-avx_presenter_tools=["pencil", "hand"]';
exports.multiUserTools = 'userdata-avx_multi_user_tools=["pencil", "hand"]';
const cssCode = '.presentationTitle--1LT79g{display: none;}';
exports.customStyle = `userdata-avx_custom_style=${cssCode}`;
exports.customStyleUrl = 'userdata-avx_custom_style_url=https://develop.averox.org/css-test-file.css';
exports.autoSwapLayout = 'userdata-avx_auto_swap_layout=true';
exports.hidePresentation = 'userdata-avx_hide_presentation=true';
exports.outsideToggleSelfVoice = 'userdata-avx_outside_toggle_self_voice=true';
exports.outsideToggleRecording = 'userdata-avx_outside_toggle_recording=true';
exports.showPublicChatOnLogin = 'userdata-avx_show_public_chat_on_login=false';
exports.forceRestorePresentationOnNewEvents = 'userdata-avx_force_restore_presentation_on_new_events=true';
exports.bannerText = 'bannerText=some text';
exports.color = 'FFFF00';
exports.bannerColor = `bannerColor=%23${this.color}`;
exports.recordMeeting = 'userdata-avx_record_video=false';
exports.skipVideoPreview = 'userdata-avx_skip_video_preview=true';
exports.skipVideoPreviewOnFirstJoin = 'userdata-avx_skip_video_preview_on_first_join=true';
exports.mirrorOwnWebcam = 'userdata-avx_mirror_own_webcam=true';
exports.showParticipantsOnLogin = 'userdata-avx_show_participants_on_login=false';

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
