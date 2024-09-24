package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonUtilTest {
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
