package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonUtilNamingTest {
    @Test
    void convertToSnakeCase_EmptyString_ReturnsEmptyString() {
        // Act
        String result = CommonUtils.Naming.convertToSnakeCase("");

        // Assert
        Assertions.assertEquals("", result);
    }

    @Test
    void convertToSnakeCase_NullString_ReturnsNull() {
        // Act
        String result = CommonUtils.Naming.convertToSnakeCase(null);

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    void convertToSnakeCase_SingleWord_ReturnsSingleWordInLowerCase() {
        // Act
        String result = CommonUtils.Naming.convertToSnakeCase("CamelCase");

        // Assert
        Assertions.assertEquals("camel_case", result);
    }

    @Test
    void convertToSnakeCase_MultiWord_ReturnsSnakeCaseString() {
        // Act
        String result = CommonUtils.Naming.convertToSnakeCase("CamelCaseExample");

        // Assert
        Assertions.assertEquals("camel_case_example", result);
    }

    @Test
    void convertToCamelCase_EmptyString_ReturnsEmptyString() {
        // Act
        String result = CommonUtils.Naming.convertToCamelCase("");

        // Assert
        Assertions.assertEquals("", result);
    }

    @Test
    void convertToCamelCase_NullString_ReturnsNull() {
        // Act
        String result = CommonUtils.Naming.convertToCamelCase(null);

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    void convertToCamelCase_SingleWord_ReturnsSameWord() {
        // Act
        String result = CommonUtils.Naming.convertToCamelCase("camel");

        // Assert
        Assertions.assertEquals("camel", result);
    }

    @Test
    void convertToCamelCase_SnakeCase_ReturnsCamelCase() {
        // Act
        String result = CommonUtils.Naming.convertToCamelCase("camel_case_example");

        // Assert
        Assertions.assertEquals("camelCaseExample", result);
    }

    @Test
    void convertToCapitalCase_EmptyString_ReturnsEmptyString() {
        // Act
        String result = CommonUtils.Naming.convertToCapitalCase("");

        // Assert
        Assertions.assertEquals("", result);
    }

    @Test
    void convertToCapitalCase_NullString_ReturnsNull() {
        // Act
        String result = CommonUtils.Naming.convertToCapitalCase(null);

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    void convertToCapitalCase_SingleWord_ReturnsCapitalizedWord() {
        // Act
        String result = CommonUtils.Naming.convertToCapitalCase("singleword");

        // Assert
        Assertions.assertEquals("Singleword", result);
    }

    @Test
    void convertToCapitalCase_SnakeCase_ReturnsCapitalizedWords() {
        // Act
        String result = CommonUtils.Naming.convertToCapitalCase("snake_case_example");

        // Assert
        Assertions.assertEquals("Snake Case Example", result);
    }
}
