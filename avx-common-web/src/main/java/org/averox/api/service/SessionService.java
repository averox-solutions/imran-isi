package org.averox.api.service;

import org.averox.api.MeetingService;
import org.averox.api.domain.UserSession;

public class SessionService {

    private String sessionToken;
    private UserSession userSession;
    private MeetingService meetingService;

    public SessionService() {
        meetingService = ServiceUtils.getMeetingService();
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        getUserSessionWithToken();
    }

    public String getSessionToken() { return sessionToken; }

    private void getUserSessionWithToken() {
        if(sessionToken != null) {
            userSession = meetingService.getUserSessionWithSessionToken(sessionToken);
        }
    }

    public String getMeetingID() {
        if(userSession != null) {
            return userSession.meetingID;
        }
        return "";
    }
}
