package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.enums.LocaleFile;
import com.dut.pbl6_server.common.enums.LocaleLanguage;
import com.dut.pbl6_server.common.util.I18nUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

public class I18nUtilTest {

    @Test
    void tr_ValidKey_ReturnsCorrectValue() {
        // Arrange
        String key = "welcome.message";
        LocaleFile localeFile = LocaleFile.APP;
        Map<String, Object> mockYAMLData = Map.of(
            "welcome", Map.of("message", "Hello, welcome!")
        );

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.getValueFromYAMLFile(LocaleLanguage.EN, localeFile))
                .thenReturn(mockYAMLData);

            mocked.when(I18nUtils::getCurrentLanguage).thenCallRealMethod();
            mocked.when(() -> I18nUtils.tr(key, localeFile)).thenCallRealMethod();

            // Act
            String result = I18nUtils.tr(key, localeFile);

            // Assert
            Assertions.assertEquals("Hello, welcome!", result);
        }
    }

    @Test
    void tr_InvalidKey_ReturnsNull() {
        // Arrange
        String key = "invalid.key";
        LocaleFile localeFile = LocaleFile.APP;
        Map<String, Object> mockYAMLData = Map.of(
            "welcome", Map.of("message", "Hello, welcome!")
        );

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.getValueFromYAMLFile(LocaleLanguage.EN, localeFile))
                .thenReturn(mockYAMLData);

            mocked.when(I18nUtils::getCurrentLanguage).thenCallRealMethod();
            mocked.when(() -> I18nUtils.tr(key, localeFile)).thenCallRealMethod();

            // Act
            String result = I18nUtils.tr(key, localeFile);

            // Assert
            Assertions.assertNull(result);
        }
    }

    @Test
    void tr_WithParams_ReplacesPlaceholdersCorrectly() {
        // Arrange
        String key = "welcome.message";
        String[] params = {"John"};
        LocaleFile localeFile = LocaleFile.APP;
        Map<String, Object> mockYAMLData = Map.of(
            "welcome", Map.of("message", "Hello, {0}!")
        );

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.getValueFromYAMLFile(LocaleLanguage.EN, localeFile))
                .thenReturn(mockYAMLData);

            mocked.when(I18nUtils::getCurrentLanguage).thenCallRealMethod();
            mocked.when(() -> I18nUtils.tr(key, localeFile)).thenCallRealMethod();
            mocked.when(() -> I18nUtils.tr(key, localeFile, params)).thenCallRealMethod();

            // Act
            String result = I18nUtils.tr(key, localeFile, params);

            // Assert
            Assertions.assertEquals("Hello, John!", result);
        }
    }

    @Test
    void tr_CurrentLanguageNull_FallsBackToDefaultLanguage() {
        // Arrange
        String key = "welcome.message";
        LocaleFile localeFile = LocaleFile.APP;
        I18nUtils.setLanguage(null);  // Clear current language

        Map<String, Object> mockYAMLData = Map.of(
            "welcome", Map.of("message", "Hello, welcome!")
        );

        try (MockedStatic<I18nUtils> mocked = Mockito.mockStatic(I18nUtils.class)) {
            mocked.when(() -> I18nUtils.getValueFromYAMLFile(LocaleLanguage.EN, localeFile))
                .thenReturn(mockYAMLData);

            mocked.when(I18nUtils::getCurrentLanguage).thenCallRealMethod();
            mocked.when(() -> I18nUtils.tr(key, localeFile)).thenCallRealMethod();

            // Act
            String result = I18nUtils.tr(key, localeFile);

            // Assert
            Assertions.assertEquals("Hello, welcome!", result);
            Assertions.assertEquals(LocaleLanguage.EN, I18nUtils.getCurrentLanguage());
        }
    }
}
