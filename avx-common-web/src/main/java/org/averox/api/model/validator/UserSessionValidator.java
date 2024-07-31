package org.averox.api.model.validator;

import org.averox.api.domain.UserSession;
import org.averox.api.model.constraint.UserSessionConstraint;
import org.averox.api.service.ServiceUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserSessionValidator implements ConstraintValidator<UserSessionConstraint, String> {

    @Override
    public void initialize(UserSessionConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String sessionToken, ConstraintValidatorContext constraintValidatorContext) {

        if(sessionToken == null) {
            return false;
        }

        UserSession userSession = ServiceUtils.getMeetingService().getUserSessionWithSessionToken(sessionToken);

        if(userSession == null) {
            return false;
        }

        return true;
    }
}
