package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CommonUtilDateTimeTest {
    @Test
    void getCurrentTimestamp_ReturnsNonNullTimestamp() {
        // Act
        Timestamp timestamp = CommonUtils.DateTime.getCurrentTimestamp();

        // Assert
        Assertions.assertNotNull(timestamp);
    }

    @Test
    void getCurrentTimestamp_TimestampReflectsCurrentTime() {
        // Act
        Timestamp timestamp = CommonUtils.DateTime.getCurrentTimestamp();
        long currentTime = System.currentTimeMillis();

        // Assert
        Assertions.assertTrue(Math.abs(currentTime - timestamp.getTime()) < 1000, "Timestamp should be within 1 second of the current time.");
    }

    @Test
    void convertTimestampToString_ValidTimestampAndFormat_ReturnsFormattedString() throws ParseException {
        // Arrange
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String expectedDateStr = sdf.format(timestamp);

        // Act
        String result = CommonUtils.DateTime.convertTimestampToString(timestamp, format);

        // Assert
        Assertions.assertEquals(expectedDateStr, result);
    }

    @Test
    void convertTimestampToString_InvalidFormat_ThrowsIllegalArgumentException() {
        // Arrange
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String invalidFormat = "invalid-format";

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.DateTime.convertTimestampToString(timestamp, invalidFormat);
        });
    }

    @Test
    void convertTimestampToString_NullTimestamp_ThrowsNullPointerException() {
        // Arrange
        String format = "yyyy-MM-dd HH:mm:ss";

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommonUtils.DateTime.convertTimestampToString(null, format);
        });
    }
}
