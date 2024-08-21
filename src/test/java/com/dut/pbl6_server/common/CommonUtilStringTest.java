package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonUtilStringTest {
    @Test
    void isValidUuid_ValidUuid_ReturnsTrue() {
        // Arrange
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";

        // Act
        boolean result = CommonUtils.String.isValidUuid(validUuid);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isValidUuid_InvalidUuid_ReturnsFalse() {
        // Arrange
        String invalidUuid = "invalid-uuid";

        // Act
        boolean result = CommonUtils.String.isValidUuid(invalidUuid);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isValidUuid_NullUuid_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.String.isValidUuid(null);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isValidUuid_EmptyUuid_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.String.isValidUuid("");

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isEmptyOrNull_NullString_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.String.isEmptyOrNull(null);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_EmptyString_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.String.isEmptyOrNull("");

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_WhitespaceString_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.String.isEmptyOrNull("   ");

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_NonEmptyString_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.String.isEmptyOrNull("Hello");

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_NullString_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.String.isNotEmptyOrNull(null);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_EmptyString_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.String.isNotEmptyOrNull("");

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_WhitespaceString_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.String.isNotEmptyOrNull("   ");

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_NonEmptyString_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.String.isNotEmptyOrNull("Hello");

        // Assert
        Assertions.assertTrue(result);
    }
}
