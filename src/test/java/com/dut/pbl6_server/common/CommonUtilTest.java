package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CommonUtilTest {
    @Test
    void getValueFromYAMLFile_ValidFile_ReturnsMap() {
        // Act
        Map<String, Object> result = CommonUtils.getValueFromYAMLFile(CommonConstants.VALIDATION_YML_FILE);

        // Assert
        Assertions.assertNotNull(result);
    }

    @Test
    void getValueFromYAMLFile_FileNotFound_ReturnsEmptyMap() {
        // Arrange
        String invalidYamlFile = "invalid-config.yaml";

        // Act
        Map<String, Object> result = CommonUtils.getValueFromYAMLFile(invalidYamlFile);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty(), "Result should be an empty map for non-existent file.");
    }

    @Test
    void stringToEnum_ValidString_ReturnsEnum() {
        // Arrange
        String enumName = "VALUE_ONE";
        Class<TestEnum> enumType = TestEnum.class;

        // Act
        TestEnum result = CommonUtils.stringToEnum(enumName, enumType);

        // Assert
        Assertions.assertEquals(TestEnum.VALUE_ONE, result);
    }

    @Test
    void stringToEnum_InvalidString_ThrowsIllegalArgumentException() {
        // Arrange
        String invalidEnumName = "INVALID";
        Class<TestEnum> enumType = TestEnum.class;

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.stringToEnum(invalidEnumName, enumType);
        });
    }

    // Dummy enum for testing
    private enum TestEnum {
        VALUE_ONE, VALUE_TWO
    }
}
