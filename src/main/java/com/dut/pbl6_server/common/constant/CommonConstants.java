package com.dut.pbl6_server.common.constant;

public final class CommonConstants {
    public static final String PROD_PROFILE = "production";
    public static final String DEV_PROFILE = "development";
    public static final String JWT_TYPE = "Bearer";
    public static final long DAY_IN_MILLIS = 86400000L;

    /*
     * File path
     */
    public static final String ACCESS_TOKEN_PRIVATE_KEY_FILE = "secret/access_token_private_key.der";
    public static final String ACCESS_TOKEN_PUBLIC_KEY_FILE = "secret/access_token_public_key.der";
    public static final String REFRESH_TOKEN_PRIVATE_KEY_FILE = "secret/refresh_token_private_key.der";
    public static final String REFRESH_TOKEN_PUBLIC_KEY_FILE = "secret/refresh_token_public_key.der";


    /*
     * Regex pattern
     */
    public static final String UUID_REGEX_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static final String PASSWORD_REGEXP_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,32}$";
    public static final String EMAIL_REGEXP_PATTERN = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
        + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$";
    public static final String I18N_REGEX_PATTERN = "<i18n>([^<]+)</i18n>";

    private CommonConstants() {
    }
}
