package org.averox.api.model.validator;

import org.averox.api.domain.Meeting;
import org.averox.api.model.constraint.MeetingEndedConstraint;
import org.averox.api.service.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MeetingEndedValidator implements ConstraintValidator<MeetingEndedConstraint, String> {

    private static Logger log = LoggerFactory.getLogger(MeetingEndedValidator.class);

    @Override
    public void initialize(MeetingEndedConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String meetingID, ConstraintValidatorContext context) {

        if(meetingID == null) {
            return false;
        }

        Meeting meeting = ServiceUtils.findMeetingFromMeetingID(meetingID);

        if(meeting == null) {
            return false;
        }

        return !meeting.isForciblyEnded();
    }
}
