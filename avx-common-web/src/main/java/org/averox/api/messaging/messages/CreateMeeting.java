package org.averox.api.messaging.messages;

import org.averox.api.domain.Meeting;

public class CreateMeeting implements IMessage {

	public final Meeting meeting;
	
	public CreateMeeting(Meeting meeting) {
		this.meeting = meeting;
	}
}
