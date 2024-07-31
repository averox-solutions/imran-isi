package org.averox.api.messaging.messages;

public class KeepAliveReply implements IMessage {
	
  public final String system;
  public final Long avxWebTimestamp;
  public final Long akkaAppsTimestamp;

  public KeepAliveReply(String system, Long avxWebTimestamp, Long akkaAppsTimestamp) {
  	this.system = system;
  	this.avxWebTimestamp = avxWebTimestamp;
  	this.akkaAppsTimestamp = akkaAppsTimestamp;
  }
}
