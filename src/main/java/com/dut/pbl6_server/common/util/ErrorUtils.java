package com.dut.pbl6_server.common.util;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.InvalidDataException;
import com.dut.pbl6_server.common.model.ErrorResponse;
import jakarta.validation.ConstraintViolation;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ErrorUtils {

    /**
     * Extract exception error
     *
     * @param error String error
     * @return ErrorResponse
     */
    @SuppressWarnings("unchecked")
    public static ErrorResponse getExceptionError(String error) {
        Map<String, Object> errors = CommonUtils.getValueFromYAMLFile(CommonConstants.ERROR_YML_FILE);
        Map<String, Object> objError = (Map<String, Object>) errors.get(error);
        String code = (String) objError.get("code");
        String message = (String) objError.get("message");
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
    @SuppressWarnings("unchecked")
    public static ErrorResponse getValidationError(String resource, String fieldName, String error) {
        if (fieldName.contains("[")) {
            fieldName = handleFieldName(fieldName);
        }
        Map<String, Object> errors = CommonUtils.getValueFromYAMLFile(CommonConstants.VALIDATION_YML_FILE);
        Map<String, Object> fields = (Map<String, Object>) errors.get(resource);
        if (fields == null)
            new ErrorResponse(ErrorMessageConstants.INTERNAL_SERVER_ERROR_CODE,
                String.format("Don't have resource: {%s} in validation.yml", resource));
        Map<String, Object> objErrors = (Map<String, Object>) Objects.requireNonNull(fields).get(fieldName);
        Map<String, Object> objError = (Map<String, Object>) objErrors.get(error);
        if (objError == null)
            new ErrorResponse(ErrorMessageConstants.INTERNAL_SERVER_ERROR_CODE, "objError is null");
        String code = (String) Objects.requireNonNull(objError).get("code");
        String message = (String) objError.get("message");
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
