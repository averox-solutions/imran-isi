package org.averox.web.services;

public class KeepAlivePong implements KeepAliveMessage {

	public final String system;
	public final Long avxWebTimestamp;
	public final Long akkaAppsTimestamp;

	public KeepAlivePong(String system, Long avxWebTimestamp, Long akkaAppsTimestamp) {
		this.system = system;
		this.avxWebTimestamp = avxWebTimestamp;
		this.akkaAppsTimestamp = akkaAppsTimestamp;
	}
}
