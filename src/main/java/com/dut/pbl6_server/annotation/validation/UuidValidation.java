package com.dut.pbl6_server.annotation.validation;

import com.dut.pbl6_server.common.constant.CommonConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint for a UUID in string format.
 * e.g. 26929514-237c-11ed-861d-0242ac120002
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = CommonConstants.UUID_REGEX_PATTERN)
@ReportAsSingleViolation
public @interface UuidValidation {
    String message() default "pbl6.validation.constraints.UuidValidation.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
