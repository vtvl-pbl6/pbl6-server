package com.dut.pbl6_server.annotation.validation;

import com.dut.pbl6_server.common.constant.CommonConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint for a password.
 * The password policy is:
 * -- 1. At least 8 chars
 * -- 2. At most 32 chars
 * -- 3. Contains at least one digit
 * -- 4. Contains at least one lower alpha char and one upper alpha char
 * -- 5. Does not contain space, tab, etc.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank
@NotNull
@Length(min = 8, max = 32)
@Pattern(regexp = CommonConstants.PASSWORD_REGEXP_PATTERN)
@ReportAsSingleViolation
@NotNull
public @interface PasswordValidation {
    String message() default "pbl6.validation.constraints.PasswordValidation.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
