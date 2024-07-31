package org.averox.api.messaging.messages;

public class UserLockedInMeeting implements IMessage {
	public final String meetingId;
	public final String userId;
	public final Boolean locked;

	public UserLockedInMeeting(String meetingId, String userId, Boolean locked) {
		this.meetingId = meetingId;
		this.userId = userId;
		this.locked = locked;
	}
}
