package org.averox.api2;

import java.util.ArrayList;
import java.util.Map;

import org.averox.api.domain.BreakoutRoomsParams;
import org.averox.api.domain.LockSettingsParams;
import org.averox.api.domain.Group;
import org.averox.api.messaging.converters.messages.DestroyMeetingMessage;
import org.averox.api.messaging.converters.messages.EndMeetingMessage;
import org.averox.api.messaging.converters.messages.PublishedRecordingMessage;
import org.averox.api.messaging.converters.messages.UnpublishedRecordingMessage;
import org.averox.api.messaging.converters.messages.DeletedRecordingMessage;
import org.averox.api.messaging.messages.ChatMessageFromApi;
import org.averox.presentation.messages.IDocConversionMsg;

public interface IBbbWebApiGWApp {
  void send(String channel, String message);
  void createMeeting(String meetingID, String externalMeetingID,
                     String parentMeetingID, String meetingName, Boolean recorded,
                     String voiceBridge, Integer duration, Boolean autoStartRecording,
                     Boolean allowStartStopRecording,
                     Boolean recordFullDurationMedia,
                     Boolean webcamsOnlyForModerator,
                     Integer meetingCameraCap,
                     Integer userCameraCap,
                     Integer maxPinnedCameras,
                     String moderatorPass,
                     String viewerPass,
                     String learningDashboardAccessToken,
                     Long createTime,
                     String createDate,
                     Boolean isBreakout,
                     Integer sequence,
                     Boolean freejoin,
                     Map<String, String> metadata,
                     String guestPolicy,
                     Boolean authenticatedGuest,
                     Boolean allowPromoteGuestToModerator,
                     Long waitingGuestUsersTimeout,
                     String meetingLayout,
                     String welcomeMsgTemplate,
                     String welcomeMsg,
                     String welcomeMsgForModerators,
                     String dialNumber,
                     Integer maxUsers,
                     Integer maxUserConcurrentAccesses,
                     Integer meetingExpireIfNoUserJoinedInMinutes,
                     Integer meetingExpireWhenLastUserLeftInMinutes,
                     Integer userInactivityInspectTimerInMinutes,
                     Integer userInactivityThresholdInMinutes,
                     Integer userActivitySignResponseDelayInMinutes,
                     Boolean endWhenNoModerator,
                     Integer endWhenNoModeratorDelayInMinutes,
                     Boolean muteOnStart,
                     Boolean allowModsToUnmuteUsers,
                     Boolean allowModsToEjectCameras,
                     Boolean keepEvents,
                     BreakoutRoomsParams breakoutParams,
                     LockSettingsParams lockSettingsParams,
                     String loginUrl,
                     String logoutUrl,
                     String customLogoURL,
                     String bannerText,
                     String bannerColor,
                     ArrayList<Group> groups,
                     ArrayList<String> disabledFeatures,
                     Boolean notifyRecordingIsOn,
                     String presentationUploadExternalDescription,
                     String presentationUploadExternalUrl,
                     String overrideClientSettings);

  void registerUser(String meetingID, String internalUserId, String fullname, String role,
                    String externUserID, String authToken, String sessionToken, String avatarURL,
                    Boolean guest, Boolean authed, String guestStatus, Boolean excludeFromDashboard,
                    String enforceLayout, Map<String, String> userMetadata);

  void destroyMeeting(DestroyMeetingMessage msg);
  void endMeeting(EndMeetingMessage msg);
  void sendKeepAlive(String system, Long avxWebTimestamp, Long akkaAppsTimestamp);
  void publishedRecording(PublishedRecordingMessage msg);
  void unpublishedRecording(UnpublishedRecordingMessage msg);
  void deletedRecording(DeletedRecordingMessage msg);
  void sendDocConversionMsg(IDocConversionMsg msg);
  void sendChatMessage(ChatMessageFromApi msg);
}