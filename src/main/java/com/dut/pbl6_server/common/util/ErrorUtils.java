package com.dut.pbl6_server.common.util;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.LocaleFile;
import com.dut.pbl6_server.common.exception.InvalidDataException;
import com.dut.pbl6_server.common.model.ErrorResponse;
import jakarta.validation.ConstraintViolation;

import java.util.Objects;
import java.util.Set;

public final class ErrorUtils {

    /**
     * Extract exception error
     *
     * @param error String error
     * @return ErrorResponse
     */
    public static ErrorResponse getExceptionError(String error) {
        String code = I18nUtils.tr(error + ".code", LocaleFile.ERROR);
        String message = I18nUtils.tr(error + ".message", LocaleFile.ERROR);
        return new ErrorResponse(code, message);
    }

    /**
     * Extract validation error
     *
     * @param resource  file error
     * @param fieldName field error
     * @param error     String error
     * @return ErrorResponse
     */
    public static ErrorResponse getValidationError(String resource, String fieldName, String error) {
        if (fieldName.contains("[")) {
            fieldName = handleFieldName(fieldName);
        }

        String code = I18nUtils.tr(String.format("%s.%s.%s.code", resource, fieldName, error), LocaleFile.VALIDATION);
        String message = I18nUtils.tr(String.format("%s.%s.%s.message", resource, fieldName, error), LocaleFile.VALIDATION);

        if (code == null || message == null) {
            return new ErrorResponse(ErrorMessageConstants.INTERNAL_SERVER_ERROR_CODE,
                String.format("Don't have resource: {%s} in %s/validations.yml", resource, I18nUtils.getCurrentLanguage().getValue()));
        }

        return new ErrorResponse(code, message);
    }

    public static void checkConstraintViolation(Set<ConstraintViolation<Object>> violations) {
        if (violations != null && !violations.isEmpty()) {
            throw Objects.requireNonNull(getInvalidDataException(violations));
        }
    }

    public static InvalidDataException getInvalidDataException(Set<ConstraintViolation<Object>> violations) {
        if (violations.isEmpty()) return null;
        ConstraintViolation<Object> violation = violations.iterator().next();
        String fieldName = CommonUtils.Naming.convertToSnakeCase(violation.getPropertyPath().toString());
        String resource = CommonUtils.Naming.convertToSnakeCase(violation.getRootBeanClass().getSimpleName());
        String[] arr = violation.getMessageTemplate().split("\\.");
        String error = CommonUtils.Naming.convertToSnakeCase(arr[arr.length - 2]);
        return new InvalidDataException(resource, fieldName, error);
    }

    private static String handleFieldName(String fieldName) {
        String index = fieldName.substring(fieldName.indexOf("[") + 1, fieldName.indexOf("]"));
        return fieldName.replaceAll(index, "");
    }

    private ErrorUtils() {
    }
}
