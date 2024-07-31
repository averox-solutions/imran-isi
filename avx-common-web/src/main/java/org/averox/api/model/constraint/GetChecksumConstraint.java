package org.averox.api.model.constraint;

import org.averox.api.model.validator.GetChecksumValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = GetChecksumValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface GetChecksumConstraint {

    String key() default "checksumError";
    String message() default "Checksums do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
