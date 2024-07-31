package org.averox.api.model.request;

import org.averox.api.model.constraint.ContentTypeConstraint;
import org.averox.api.model.constraint.MeetingExistsConstraint;
import org.averox.api.model.constraint.MeetingIDConstraint;
import org.averox.api.model.shared.Checksum;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ContentTypeConstraint
public class MeetingInfo extends RequestWithChecksum<MeetingInfo.Params> {

    public enum Params implements RequestParameters {
        MEETING_ID("meetingID");

        private final String value;

        Params(String value) { this.value = value; }

        public String getValue() { return value; }
    }

    @MeetingIDConstraint
    @MeetingExistsConstraint
    private String meetingID;

    public MeetingInfo(Checksum checksum, HttpServletRequest servletRequest) {
        super(checksum, servletRequest);
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    @Override
    public void populateFromParamsMap(Map<String, String[]> params) {
        if(params.containsKey(Params.MEETING_ID.getValue())) {
            setMeetingID(params.get(Params.MEETING_ID.getValue())[0]);
        }
    }
}
