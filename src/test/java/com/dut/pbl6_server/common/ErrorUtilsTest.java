package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.LocaleFile;
import com.dut.pbl6_server.common.enums.LocaleLanguage;
import com.dut.pbl6_server.common.model.ErrorResponse;
import com.dut.pbl6_server.common.util.ErrorUtils;
import com.dut.pbl6_server.common.util.I18nUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ErrorUtilsTest {
    @Test
    void getExceptionError_ValidError_ReturnsErrorResponse() {
        // Arrange
        String error = "test.error";
        String expectedCode = "ERR001";
        String expectedMessage = "An error occurred";

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.tr("test.error.code", LocaleFile.ERROR))
                .thenReturn(expectedCode);
            mocked.when(() -> I18nUtils.tr("test.error.message", LocaleFile.ERROR))
                .thenReturn(expectedMessage);

            // Act
            ErrorResponse result = ErrorUtils.getExceptionError(error);

            // Assert
            Assertions.assertEquals(expectedCode, result.getCode());
            Assertions.assertEquals(expectedMessage, result.getMessage());
        }
    }

    @Test
    void getExceptionError_NullValues_ReturnsErrorResponseWithNullFields() {
        // Arrange
        String error = "unknown.error";

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.tr("unknown.error.code", LocaleFile.ERROR))
                .thenReturn(null);
            mocked.when(() -> I18nUtils.tr("unknown.error.message", LocaleFile.ERROR))
                .thenReturn(null);

            // Act
            ErrorResponse result = ErrorUtils.getExceptionError(error);

            // Assert
            Assertions.assertNull(result.getCode());
            Assertions.assertNull(result.getMessage());
        }
    }

    @Test
    void getValidationError_ValidField_ReturnsErrorResponse() {
        // Arrange
        String resource = "user";
        String fieldName = "username";
        String error = "required";
        String expectedCode = "VAL001";
        String expectedMessage = "Username is required";

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.tr("user.username.required.code", LocaleFile.VALIDATION))
                .thenReturn(expectedCode);
            mocked.when(() -> I18nUtils.tr("user.username.required.message", LocaleFile.VALIDATION))
                .thenReturn(expectedMessage);

            // Act
            ErrorResponse result = ErrorUtils.getValidationError(resource, fieldName, error);

            // Assert
            Assertions.assertEquals(expectedCode, result.getCode());
            Assertions.assertEquals(expectedMessage, result.getMessage());
        }
    }

    @Test
    void getValidationError_ResourceNotFound_ReturnsInternalServerError() {
        // Arrange
        String resource = "nonexistent";
        String fieldName = "username";
        String error = "required";

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(I18nUtils::getCurrentLanguage).thenReturn(LocaleLanguage.EN);
            mocked.when(() -> I18nUtils.tr("nonexistent.username.required.code", LocaleFile.VALIDATION))
                .thenReturn(null);
            mocked.when(() -> I18nUtils.tr("nonexistent.username.required.message", LocaleFile.VALIDATION))
                .thenReturn(null);

            // Act
            ErrorResponse result = ErrorUtils.getValidationError(resource, fieldName, error);

            // Assert
            Assertions.assertEquals(ErrorMessageConstants.INTERNAL_SERVER_ERROR_CODE, result.getCode());
            Assertions.assertEquals("Don't have resource: {nonexistent} in en/validations.yml", result.getMessage());
        }
    }

    @Test
    void getValidationError_FieldNameWithBrackets_HandlesCorrectly() {
        // Arrange
        String resource = "user";
        String fieldName = "usernames[0]";
        String processedFieldName = "usernames[]"; // Assume this is how handleFieldName would process it
        String error = "required";
        String expectedCode = "VAL001";
        String expectedMessage = "Username at index 0 is required";

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(I18nUtils::getCurrentLanguage).thenReturn(LocaleLanguage.EN);
            mocked.when(() -> I18nUtils.tr("user.usernames[].required.code", LocaleFile.VALIDATION))
                .thenReturn(expectedCode);
            mocked.when(() -> I18nUtils.tr("user.usernames[].required.message", LocaleFile.VALIDATION))
                .thenReturn(expectedMessage);

            // Act
            ErrorResponse result = ErrorUtils.getValidationError(resource, fieldName, error);

            // Assert
            Assertions.assertEquals(expectedCode, result.getCode());
            Assertions.assertEquals(expectedMessage, result.getMessage());
        }
    }
}
