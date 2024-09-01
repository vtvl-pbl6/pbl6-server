package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.model.ErrorResponse;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.ErrorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

public class ErrorUtilsTest {
    private MockedStatic<CommonUtils> commonUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        // Initialize static mocking
        commonUtilsMockedStatic = Mockito.mockStatic(CommonUtils.class);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock
        commonUtilsMockedStatic.close();
    }

    @Test
    void getExceptionError_ValidError_ReturnsErrorResponse() {
        // Arrange
        String error = "someError";
        Map<String, Object> mockErrors = Map.of(
            error, Map.of(
                "code", "404",
                "message", "Not Found"
            )
        );
        commonUtilsMockedStatic.when(() -> CommonUtils.getValueFromYAMLFile(CommonConstants.ERROR_YML_FILE)).thenReturn(mockErrors);

        // Act
        ErrorResponse response = ErrorUtils.getExceptionError(error);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("404", response.getCode());
        Assertions.assertEquals("Not Found", response.getMessage());
    }

    @Test
    void getValidationError_ValidData_ReturnsErrorResponse() {
        // Arrange
        String resource = "user";
        String fieldName = "username";
        String error = "required";
        Map<String, Object> mockErrors = Map.of(
            resource, Map.of(
                fieldName, Map.of(
                    error, Map.of(
                        "code", "400",
                        "message", "Username is required"
                    )
                )
            )
        );
        commonUtilsMockedStatic.when(() -> CommonUtils.getValueFromYAMLFile(CommonConstants.VALIDATION_YML_FILE)).thenReturn(mockErrors);

        // Act
        ErrorResponse response = ErrorUtils.getValidationError(resource, fieldName, error);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400", response.getCode());
        Assertions.assertEquals("Username is required", response.getMessage());
    }

//    @Test
//    void getValidationError_FieldNameWithIndex_ReturnsErrorResponse() {
//        // Arrange
//        String resource = "user";
//        String fieldName = "items[0].username";
//        String error = "required";
//        Map<String, Object> mockErrors = Map.of(
//            resource, Map.of(
//                "items.username", Map.of(
//                    error, Map.of(
//                        "code", "400",
//                        "message", "Username is required"
//                    )
//                )
//            )
//        );
//        commonUtilsMockedStatic.when(() -> CommonUtils.getValueFromYAMLFile(CommonConstants.VALIDATION_YML_FILE)).thenReturn(mockErrors);
//
//        // Act
//        ErrorResponse response = ErrorUtils.getValidationError(resource, fieldName, error);
//
//        // Assert
//        Assertions.assertNotNull(response);
//        Assertions.assertEquals("400", response.getCode());
//        Assertions.assertEquals("Username is required", response.getMessage());
//    }
}
