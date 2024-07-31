package org.averox.api2;

import java.util.Collection;
import java.util.Map;

import org.averox.api.domain.Meeting;
import org.averox.api.domain.UserSession;
import org.averox.api.messaging.messages.UserJoinedVoice;
import org.averox.api.messaging.messages.UserLeftVoice;
import org.averox.api.messaging.messages.UserListeningOnly;
import org.averox.api.messaging.messages.UserSharedWebcam;
import org.averox.api.messaging.messages.UserUnsharedWebcam;

public interface IMeetingService {
  void addUserSession(String token, UserSession user);
  void registerUser(String meetingID, String internalUserId,
                    String fullname, String role, String externUserID,
                    String authToken, String avatarURL, Boolean guest, Boolean authed);
  UserSession getUserSession(String token);
  UserSession removeUserSession(String token);
  void purgeRegisteredUsers();
  Collection<Meeting> getMeetings();
  Collection<UserSession> getSessions();
  boolean createMeeting(Meeting m);
  Meeting getMeeting(String meetingId);
  Collection<Meeting> getMeetingsWithId(String meetingId);
  Meeting getNotEndedMeetingWithId(String meetingId);



  boolean isMeetingWithVoiceBridgeExist(String voiceBridge);
  void send(String channel, String message);
  void endMeeting(String meetingId);
  void addUserCustomData(String meetingId, String userID,
                         Map<String, String> userCustomData);
  void userJoinedVoice(UserJoinedVoice message);
  void userLeftVoice(UserLeftVoice message);
  void userListeningOnly(UserListeningOnly message);
  void userSharedWebcam(UserSharedWebcam message);
  void userUnsharedWebcam(UserUnsharedWebcam message);


}
