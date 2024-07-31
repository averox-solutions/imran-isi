package org.averox.api.messaging.messages;

public class MeetingDestroyed implements IMessage {
  public final String meetingId;
  
  public MeetingDestroyed(String meetingId) {
  	this.meetingId = meetingId;
  }
}
