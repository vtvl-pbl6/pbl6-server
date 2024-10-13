package com.dut.pbl6_server.common.util;

import com.dut.pbl6_server.common.enums.LocaleFile;
import com.dut.pbl6_server.common.enums.LocaleLanguage;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public final class I18nUtils {
    private static final ThreadLocal<LocaleLanguage> lang = new ThreadLocal<>();
    public static final LocaleLanguage DEFAULT_LANGUAGE = LocaleLanguage.VI;

    public static void setLanguage(String language) {
        try {
            lang.set(CommonUtils.stringToEnum(language, LocaleLanguage.class));
        } catch (Exception e) {
            lang.remove();
        }
    }

    public static boolean notSetLanguage() {
        return lang.get() == null;
    }

    public static LocaleLanguage getCurrentLanguage() {
        return (lang.get() == null) ? DEFAULT_LANGUAGE : lang.get();
    }

    @SuppressWarnings("unchecked")
    public static String tr(String key, LocaleFile localeFile) {
        Map<String, Object> map = getValueFromYAMLFile(getCurrentLanguage(), localeFile);
        String[] keys = key.split("\\.");
        try {
            for (int i = 0; i < keys.length; i++) {
                if (i == keys.length - 1) {
                    return map.get(keys[i]).toString();
                }
                map = (Map<String, Object>) map.get(keys[i]);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String tr(String key, LocaleFile localeFile, String... params) {
        String value = tr(key, localeFile);
        if (CommonUtils.String.isNotEmptyOrNull(value)) {
            for (int i = 0; i < params.length; i++) {
                value = value.replace("{" + i + "}", params[i]);
            }
            return value;
        }
        return null;
    }

    public static Map<String, Object> getValueFromYAMLFile(LocaleLanguage locale, LocaleFile nameFile) {
        Yaml yaml = new Yaml();
        try {
            InputStream inputStream = new ClassPathResource(String.format("i18n/%s/%s.yml", locale.getValue(), nameFile.getValue())).getInputStream();
            return yaml.load(inputStream);
        } catch (Exception e) {
            return Map.of();
        }
    }

    // Make sure to remove the value when done to avoid memory leaks
    public static void clear() {
        lang.remove();
    }

    private I18nUtils() {
    }
}
