package org.averox.api.model.validator;

import org.averox.api.model.constraint.IsIntegralConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsIntegralValidator implements ConstraintValidator<IsIntegralConstraint, String> {

    @Override
    public void initialize(IsIntegralConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if(value == null || value.equals("")) {
            return true;
        }

        try {
            Long.parseLong(value);
        } catch(NumberFormatException e) {
            return false;
        }

        return true;
    }
}
