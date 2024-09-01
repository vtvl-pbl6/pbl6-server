package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonUtilOTPTest {
    @Test
    void generate4Digits_Returns4DigitOTP() {
        // Act
        String otp = CommonUtils.OTP.generate4Digits();

        // Assert
        Assertions.assertNotNull(otp, "OTP should not be null.");
        Assertions.assertEquals(4, otp.length(), "OTP should be 4 digits long.");
        Assertions.assertTrue(otp.matches("\\d{4}"), "OTP should contain only digits.");
    }

    @Test
    void generate6Digits_Returns6DigitOTP() {
        // Act
        String otp = CommonUtils.OTP.generate6Digits();

        // Assert
        Assertions.assertNotNull(otp, "OTP should not be null.");
        Assertions.assertEquals(6, otp.length(), "OTP should be 6 digits long.");
        Assertions.assertTrue(otp.matches("\\d{6}"), "OTP should contain only digits.");
    }

    @Test
    void generate8Digits_Returns8DigitOTP() {
        // Act
        String otp = CommonUtils.OTP.generate8Digits();

        // Assert
        Assertions.assertNotNull(otp, "OTP should not be null.");
        Assertions.assertEquals(8, otp.length(), "OTP should be 8 digits long.");
        Assertions.assertTrue(otp.matches("\\d{8}"), "OTP should contain only digits.");
    }

    @Test
    void generateDigits_ValidLength_ReturnsCorrectLengthOTP() {
        // Arrange
        int length = 5; // Test with arbitrary length

        // Act
        String otp = CommonUtils.OTP.generateDigits(length);

        // Assert
        Assertions.assertNotNull(otp, "OTP should not be null.");
        Assertions.assertEquals(length, otp.length(), "OTP should match the requested length.");
        Assertions.assertTrue(otp.matches("\\d{" + length + "}"), "OTP should contain only digits.");
    }

    @Test
    void generateDigits_NegativeLength_ReturnsEmptyString() {
        // Act
        String otp = CommonUtils.OTP.generateDigits(-1);

        // Assert
        Assertions.assertNotNull(otp, "OTP should not be null.");
        Assertions.assertEquals("", otp, "OTP should be an empty string for negative length.");
    }
}
