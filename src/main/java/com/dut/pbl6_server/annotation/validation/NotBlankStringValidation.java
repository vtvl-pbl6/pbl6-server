package com.dut.pbl6_server.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank
@NotNull
@ReportAsSingleViolation
public @interface NotBlankStringValidation {
    String message() default "pbl6.validation.constraints.NotBlankStringValidation.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
