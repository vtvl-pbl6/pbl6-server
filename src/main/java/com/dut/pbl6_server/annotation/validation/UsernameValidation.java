package com.dut.pbl6_server.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint for a username.
 * The username policy is:
 * -- 1. Must not be blank
 * -- 2. Must be longer than 5 characters
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank
@Size(min = 6)
@ReportAsSingleViolation
public @interface UsernameValidation {
    String message() default "pbl6.validation.constraints.UsernameValidation.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
