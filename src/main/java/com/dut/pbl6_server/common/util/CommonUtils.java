package com.dut.pbl6_server.common.util;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public final class CommonUtils {

    /*
     * RSAKeyLoader util
     */
    public final static class RSAKeyLoader {

        public static PublicKey loadPublicKey(java.lang.String path) throws Exception {
            ClassPathResource resource = new ClassPathResource(path);
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] keyBytes = inputStream.readAllBytes();
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                return keyFactory.generatePublic(keySpec);
            }
        }

        public static PrivateKey loadPrivateKey(java.lang.String path) throws Exception {
            ClassPathResource resource = new ClassPathResource(path);
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] keyBytes = inputStream.readAllBytes();
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                return kf.generatePrivate(keySpec);
            }
        }

        private RSAKeyLoader() {
        }
    }

    /*
     * Json util
     */
    public final static class Json {
        public static java.lang.String encode(Object object) {
            try {
                if (object == null) return null;
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        public static <T> T decode(java.lang.String json, Class<T> type) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        public static <T> T decode(Map<java.lang.String, Object> map, Class<T> type) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.convertValue(map, type);
            } catch (Exception e) {
                return null;
            }
        }

        public static <T> java.util.List<T> decode(java.util.List<Map<java.lang.String, Object>> array, Class<T> type) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return array.stream().map(e -> mapper.convertValue(e, type)).toList();
            } catch (Exception e) {
                return null;
            }
        }

        public static Map<java.lang.String, Object> convertObjectToMap(Object object) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.convertValue(object, new TypeReference<Map<java.lang.String, Object>>() {
                });
            } catch (Exception e) {
                return null;
            }
        }

        private Json() {
        }
    }

    /*
     * String util
     */
    public final static class String {
        public static boolean isValidUuid(java.lang.String str) {
            return isNotEmptyOrNull(str) && Pattern.matches(CommonConstants.UUID_REGEX_PATTERN, str);
        }

        public static boolean isEmptyOrNull(java.lang.String str) {
            return str == null || str.trim().isEmpty();
        }

        public static boolean isNotEmptyOrNull(java.lang.String str) {
            return str != null && !str.trim().isEmpty();
        }

        private String() {
        }
    }

    /*
     * List util
     */
    public final static class List {
        public static boolean isEmptyOrNull(Object[] list) {
            return list == null || list.length == 0;
        }

        public static boolean isNotEmptyOrNull(Object[] list) {
            return list != null && list.length > 0;
        }

        public static boolean isEmptyOrNull(java.util.List<?> list) {
            return list == null || list.isEmpty();
        }

        public static boolean isNotEmptyOrNull(java.util.List<?> list) {
            return list != null && !list.isEmpty();
        }

        public static boolean isList(Object obj) {
            return obj != null && (obj.getClass().isArray() || obj instanceof Collection<?>);
        }

        private List() {
        }
    }

    /*
     * Naming util
     */
    public static class Naming {
        /**
         * Convert snake case
         *
         * @param input string
         * @return String type snake case
         */
        public static java.lang.String convertToSnakeCase(java.lang.String input) {
            if (String.isEmptyOrNull(input)) return input;
            return input.replaceAll("([^_A-Z])([A-Z])", "$1_$2").toLowerCase();
        }

        /**
         * Convert camel case
         *
         * @param input string
         * @return String type camel case
         */
        public static java.lang.String convertToCamelCase(java.lang.String input) {
            if (String.isEmptyOrNull(input)) return input;
            java.lang.String[] words = input.split("_");
            if (words.length == 1) return input;
            java.lang.String result = Arrays.stream(words).map(
                word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()
            ).reduce((a, b) -> a + b).orElse("");
            return result.substring(0, 1).toLowerCase() + result.substring(1); // Lowercase first character
        }

        /**
         * Convert capital case
         */
        public static java.lang.String convertToCapitalCase(java.lang.String input) {
            if (String.isEmptyOrNull(input)) return input;
            java.lang.String snakeCase = convertToSnakeCase(input);
            java.lang.String[] words = snakeCase.split("_");
            return Arrays.stream(words).map(
                word -> word.substring(0, 1).toUpperCase() + word.substring(1)
            ).reduce((a, b) -> a + " " + b).orElse("");
        }

        private Naming() {
        }
    }


    /*
     * Date time util
     */
    public final static class DateTime {
        public static Timestamp getCurrentTimestamp() {
            Date date = new Date();
            return new Timestamp(date.getTime());
        }

        public static java.lang.String convertTimestampToString(Timestamp timestamp, java.lang.String format) {
            return new SimpleDateFormat(format).format(timestamp);
        }

        private DateTime() {
        }
    }

    /*
     * Generate OTP
     */
    public final static class OTP {
        public static java.lang.String generate4Digits() {
            return generateDigits(4);
        }

        public static java.lang.String generate6Digits() {
            return generateDigits(6);
        }

        public static java.lang.String generate8Digits() {
            return generateDigits(8);
        }

        public static java.lang.String generateDigits(int length) {
            Random random = new Random();
            StringBuilder otp = new StringBuilder();
            for (int i = 1; i <= length; i++) {
                int randomNumber = random.nextInt(10);
                otp.append(randomNumber);
            }
            return otp.toString();
        }

        private OTP() {
        }
    }

    /*
     * YAML file util
     */
    public static Map<java.lang.String, Object> getValueFromYAMLFile(java.lang.String nameFile) {
        Yaml yaml = new Yaml();
        try {
            InputStream inputStream = new ClassPathResource(nameFile).getInputStream();
            return yaml.load(inputStream);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /*
     * Enum value
     */
    public static <T extends Enum<T>> T stringToEnum(java.lang.String name, Class<T> type) {
        return Enum.valueOf(type, name.toUpperCase());
    }

    private CommonUtils() {
    }
}
