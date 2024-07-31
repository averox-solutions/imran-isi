package org.averox.api.model.validator;

import org.averox.api.MeetingService;
import org.averox.api.domain.GuestPolicy;
import org.averox.api.domain.UserSession;
import org.averox.api.model.constraint.GuestPolicyConstraint;
import org.averox.api.service.ServiceUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GuestPolicyValidator implements ConstraintValidator<GuestPolicyConstraint, String> {

    @Override
    public void initialize(GuestPolicyConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String sessionToken, ConstraintValidatorContext constraintValidatorContext) {

        if(sessionToken == null) {
            return false;
        }

        MeetingService meetingService = ServiceUtils.getMeetingService();
        UserSession userSession = meetingService.getUserSessionWithSessionToken(sessionToken);

        if(userSession == null || !userSession.guestStatus.equals(GuestPolicy.ALLOW)) {
            return false;
        }

        return true;
    }
}
