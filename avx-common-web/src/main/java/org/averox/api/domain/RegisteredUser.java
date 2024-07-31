package org.averox.api.domain;

public class RegisteredUser {
    public final String authToken;
    public final String userId;
    public final Long registeredOn;

    private String guestStatus;
    private Boolean excludeFromDashboard;
    private Long guestWaitedOn;
    private Boolean leftGuestLobby;
    private String enforceLayout;

    public RegisteredUser(String authToken, String userId, String guestStatus, Boolean excludeFromDashboard,
                          Boolean leftGuestLobby, String enforceLayout) {
        this.authToken = authToken;
        this.userId = userId;
        this.guestStatus = guestStatus;
        this.excludeFromDashboard = excludeFromDashboard;
        this.leftGuestLobby = leftGuestLobby;
        this.enforceLayout = enforceLayout;

        Long currentTimeMillis = System.currentTimeMillis();
        this.registeredOn = currentTimeMillis;
        this.guestWaitedOn = currentTimeMillis;
    }

    public void setGuestStatus(String status) {
        this.guestStatus = status;
    }

    public String getGuestStatus() {
        return guestStatus;
    }

    public void setLeftGuestLobby(boolean bool) {
        this.leftGuestLobby = bool;
    }

    public Boolean getLeftGuestLobby() {
        return leftGuestLobby;
    }

    public void setExcludeFromDashboard(Boolean excludeFromDashboard) {
        this.excludeFromDashboard = excludeFromDashboard;
    }

    public String getEnforceLayout() {
        return enforceLayout;
    }

    public void setEnforceLayout(String enforceLayout) {
        this.enforceLayout = enforceLayout;
    }

    public Boolean getExcludeFromDashboard() {
        return excludeFromDashboard;
    }

    public void updateGuestWaitedOn() {
        this.guestWaitedOn = System.currentTimeMillis();
    }

    public Long getGuestWaitedOn() {
        return this.guestWaitedOn;
    }
}