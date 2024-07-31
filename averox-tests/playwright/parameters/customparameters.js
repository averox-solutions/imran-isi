const { expect, default: test } = require('@playwright/test');
const { MultiUsers } = require('../user/multiusers');
const e = require('../core/elements');
const c = require('./constants');
const { VIDEO_LOADING_WAIT_TIME, ELEMENT_WAIT_LONGER_TIME, ELEMENT_WAIT_EXTRA_LONG_TIME } = require('../core/constants');
const util = require('./util');
const { getSettings } = require('../core/settings');

class CustomParameters extends MultiUsers {
  constructor(browser, context) {
    super(browser, context);
  }

  async showPublicChatOnLogin() {
    await this.modPage.waitForSelector(e.actions);
    await this.modPage.wasRemoved(e.hidePublicChat);
  }

  async recordMeeting() {
    await this.modPage.hasElement(e.recordingIndicator);
  }

  async showParticipantsOnLogin() {
    await this.modPage.wasRemoved(e.usersList);
  }

  async clientTitle() {
    const pageTitle = await this.modPage.page.title();
    expect(pageTitle).toContain(`${c.docTitle} - `);
  }

  async askForFeedbackOnLogout() {
    await this.modPage.logoutFromMeeting();
    await this.modPage.waitForSelector(e.meetingEndedModal);
    await this.modPage.hasElement(e.rating);
  }

  async displayBrandingArea() {
    await this.modPage.waitForSelector(e.userListContent);
    await this.modPage.hasElement(e.brandingAreaLogo);
  }

  async shortcuts() {
    // Check the initial shortcuts that can be used right after joining the meeting
    await util.checkShortcutsArray(this.modPage, c.initialShortcuts);
    // Join audio
    await this.modPage.waitAndClick(e.joinAudio);
    await this.modPage.joinMicrophone();
    // Open private chat
    await this.modPage.waitAndClick(e.userListItem);
    await this.modPage.waitAndClick(e.startPrivateChat);
    await this.modPage.waitForSelector(e.hidePrivateChat);
    // Check the later shortcuts that can be used after joining audio and opening private chat
    await util.checkShortcutsArray(this.modPage, c.laterShortcuts);
  }

  async customStyle() {
    await this.modPage.waitForSelector(e.chatButton);
    const resp = await this.modPage.page.evaluate((elem) => {
      return document.querySelectorAll(elem)[0].offsetHeight == 0;
    }, e.presentationTitle);
    expect(resp).toBeTruthy();
  }

  async autoSwapLayout() {
    await this.modPage.waitForSelector(e.actions);
    await this.modPage.waitAndClick(e.minimizePresentation);
    const resp = await this.modPage.page.evaluate((elem) => {
      return document.querySelectorAll(elem)[0].offsetHeight !== 0;
    }, e.restorePresentation);
    expect(resp).toBeTruthy();
  }

  async autoJoin() {
    await this.modPage.waitForSelector(e.chatMessages, ELEMENT_WAIT_LONGER_TIME);
    await this.modPage.wasRemoved(e.audioModal);
  }

  async listenOnlyMode() {
    await this.modPage.waitForSelector(e.audioSettingsModal, ELEMENT_WAIT_EXTRA_LONG_TIME);
    await this.modPage.waitAndClick(e.joinEchoTestButton);
    await this.modPage.waitForSelector(e.establishingAudioLabel);
    await this.modPage.waitForSelector(e.isTalking);
    await this.modPage.leaveAudio();
    await this.modPage.waitAndClick(e.joinAudio);
    await this.modPage.waitForSelector(e.audioSettingsModal);
  }

  async forceListenOnly() {
    await this.userPage.wasRemoved(e.audioModal);
    await this.userPage.waitForSelector(e.toastContainer, ELEMENT_WAIT_LONGER_TIME);
    await util.forceListenOnly(this.userPage);
  }

  async skipCheck() {
    await this.modPage.waitAndClick(e.microphoneButton);
    await this.modPage.waitForSelector(e.establishingAudioLabel);
    await this.modPage.wasRemoved(e.establishingAudioLabel, ELEMENT_WAIT_LONGER_TIME);
    await this.modPage.hasElement(e.isTalking);
  }

  async skipCheckOnFirstJoin() {
    await this.modPage.waitAndClick(e.microphoneButton, ELEMENT_WAIT_LONGER_TIME);
    await this.modPage.hasElement(e.establishingAudioLabel);
    await this.modPage.hasElement(e.smallToastMsg);
    await this.modPage.hasElement(e.isTalking);
    await this.modPage.leaveAudio();
    await this.modPage.waitAndClick(e.joinAudio);
    await this.modPage.waitAndClick(e.microphoneButton);
    await this.modPage.hasElement(e.audioSettingsModal);
  }

  async skipEchoTestIfPreviousDevice() {
    await this.modPage.joinMicrophone();
    await this.modPage.leaveAudio();
    await this.modPage.waitAndClick(e.joinAudio);
    await this.modPage.waitAndClick(e.microphoneButton);
    await this.modPage.waitForSelector(e.establishingAudioLabel);
    await this.modPage.wasRemoved(e.establishingAudioLabel, ELEMENT_WAIT_LONGER_TIME);
    await this.modPage.hasElement(e.isTalking);
  }

  async bannerText() {
    await this.modPage.waitForSelector(e.actions);
    await this.modPage.hasElement(e.notificationBannerBar);
  }

  async bannerColor(colorToRGB) {
    await this.modPage.waitForSelector(e.notificationBannerBar);
    const notificationLocator = this.modPage.getLocator(e.notificationBannerBar);
    const notificationBarColor = await notificationLocator.evaluate((elem) => {
      return getComputedStyle(elem).backgroundColor;
    }, e.notificationBannerBar);
    expect(notificationBarColor).toBe(colorToRGB);
  }

  async hidePresentationOnJoin() {
    await this.modPage.waitForSelector(e.actions);
    await this.modPage.hasElement(e.restorePresentation);
    await this.userPage.hasElement(e.restorePresentation);
    await this.userPage.wasRemoved(e.whiteboard);
  }

  async forceRestorePresentationOnNewEvents(joinParameter) {
    await this.initUserPage(true, this.context, { useModMeetingId: true, joinParameter });
    const { presentationHidden, pollEnabled } = getSettings();
    if (!presentationHidden) await this.userPage.waitAndClick(e.minimizePresentation);
    const zoomInCase = await util.zoomIn(this.modPage);
    expect(zoomInCase).toBeTruthy();
    const zoomOutCase = await util.zoomOut(this.modPage);
    expect(zoomOutCase).toBeTruthy();
    if (pollEnabled) await util.poll(this.modPage, this.userPage);
    await util.nextSlide(this.modPage);
    await util.previousSlide(this.modPage);
    await util.annotation(this.modPage);
    await this.userPage.checkElement(e.restorePresentation);
  }

  async forceRestorePresentationOnNewPollResult(joinParameter) {
    await this.initUserPage(true, this.context, { useModMeetingId: true, joinParameter })
    const { presentationHidden, pollEnabled } = getSettings();
    if (!presentationHidden) await this.userPage.waitAndClick(e.minimizePresentation);
    if (pollEnabled) await util.poll(this.modPage, this.userPage);
    await this.userPage.waitForSelector(e.smallToastMsg);
    await this.userPage.checkElement(e.restorePresentation);
  }

  async enableVideo() {
    await this.modPage.wasRemoved(e.joinVideo);
  }

  async skipVideoPreview() {
    await this.modPage.shareWebcam(false);
  }

  async skipVideoPreviewOnFirstJoin() {
    await this.modPage.shareWebcam(false);
    await this.modPage.waitAndClick(e.leaveVideo, VIDEO_LOADING_WAIT_TIME);
    await this.modPage.waitForSelector(e.joinVideo);
    const { videoPreviewTimeout } = this.modPage.settings;
    await this.modPage.shareWebcam(true, videoPreviewTimeout);
  }

  async skipVideoPreviewIfPreviousDevice() {
    await this.modPage.waitForSelector(e.joinVideo);
    const { videoPreviewTimeout } = this.modPage.settings;
    await this.modPage.shareWebcam(true, videoPreviewTimeout);
    await this.modPage.waitAndClick(e.leaveVideo, VIDEO_LOADING_WAIT_TIME);
    await this.modPage.shareWebcam(false);
  }

  async mirrorOwnWebcam() {
    await this.modPage.waitAndClick(e.joinVideo);
    await this.modPage.waitForSelector(e.webcamMirroredVideoPreview);
    await this.modPage.waitAndClick(e.startSharingWebcam);
    await this.modPage.hasElement(e.webcamMirroredVideoContainer);
  }

  async multiUserPenOnly() {
    await this.modPage.waitAndClick(e.multiUsersWhiteboardOn);
    await this.userPage.hasElement(e.wbToolbar);
    const toolsCount = await this.userPage.getSelectorCount(`${e.wbToolbar} button:visible`);
    await expect(toolsCount, 'should display only 1 tool (pen)').toBe(1);
  }

  async presenterTools() {
    await this.modPage.waitForSelector(e.whiteboard, ELEMENT_WAIT_LONGER_TIME);
    await this.modPage.hasElement(e.wbToolbar);
    const toolsCount = await this.modPage.getSelectorCount(`${e.wbToolbar} button:visible`);
    await expect(toolsCount, 'should display only the 3 elements passed in the parameter (select, draw and arrow)').toBe(3);
  }

  async multiUserTools() {
    await this.modPage.waitAndClick(e.multiUsersWhiteboardOn);
    await this.userPage.hasElement(e.wbToolbar);
    const toolsCount = await this.userPage.getSelectorCount(`${e.wbToolbar} button:visible`);
    await expect(toolsCount, 'should display only the 2 elements passed in the parameter (arrow and text)').toBe(2);
  }

  async autoShareWebcam() {
    await this.modPage.hasElement(e.webcamSettingsModal);
  }

  async hideActionsBarTest() {
    await this.modPage.wasRemoved(e.actions);
    await this.modPage.wasRemoved(e.joinAudio);
    await this.modPage.wasRemoved(e.joinVideo);
    await this.modPage.wasRemoved(e.startScreenSharing);
    await this.modPage.wasRemoved(e.minimizePresentation);
    await this.modPage.wasRemoved(e.reactionsButton);
  }

  async overrideDefaultLocaleTest() {
    await this.modPage.hasText(e.chatButton, 'Bate-papo público');
  }

  async hideNavBarTest() {
    await this.modPage.wasRemoved(e.navbarBackground);
  }

  async preferredCameraProfileTest() {
    await this.modPage.waitAndClick(e.joinVideo);
    expect(await this.modPage.getLocator(e.selectCameraQualityId).inputValue()).toBe('low');
    await this.modPage.waitAndClick(e.startSharingWebcam);
  }
}

exports.CustomParameters = CustomParameters;