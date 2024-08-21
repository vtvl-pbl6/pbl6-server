package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtilListTest {
    @Test
    void isEmptyOrNull_ArrayNull_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isEmptyOrNull((Object[]) null);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_ArrayEmpty_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isEmptyOrNull(new Object[0]);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_ArrayNonEmpty_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isEmptyOrNull(new Object[]{1, 2, 3});

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_ArrayNull_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isNotEmptyOrNull((Object[]) null);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_ArrayEmpty_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isNotEmptyOrNull(new Object[0]);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_ArrayNonEmpty_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isNotEmptyOrNull(new Object[]{1, 2, 3});

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_ListNull_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isEmptyOrNull((List<?>) null);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_ListEmpty_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isEmptyOrNull(new ArrayList<>());

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isEmptyOrNull_ListNonEmpty_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isEmptyOrNull(Arrays.asList(1, 2, 3));

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_ListNull_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isNotEmptyOrNull((List<?>) null);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_ListEmpty_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isNotEmptyOrNull(new ArrayList<>());

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isNotEmptyOrNull_ListNonEmpty_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isNotEmptyOrNull(Arrays.asList(1, 2, 3));

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isList_Array_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isList(new Object[]{1, 2, 3});

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isList_List_ReturnsTrue() {
        // Act
        boolean result = CommonUtils.List.isList(Arrays.asList(1, 2, 3));

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void isList_NonCollectionOrArray_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isList("Not a list or array");

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void isList_Null_ReturnsFalse() {
        // Act
        boolean result = CommonUtils.List.isList(null);

        // Assert
        Assertions.assertFalse(result);
    }
}
