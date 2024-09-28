package com.dut.pbl6_server.common.constant;

public final class ErrorMessageConstants {

    public static final String INTERNAL_SERVER_ERROR_CODE = "ERR_SER0101";
    public static final String BAD_REQUEST_ERROR_CODE = "ERR_SER0102";

    /**
     * Common
     */
    public static final String INTERNAL_SERVER_ERROR = "internal_server_error";
    public static final String PAGE_NOT_FOUND = "page_not_found";
    public static final String FORBIDDEN = "forbidden";
    public static final String UNAUTHORIZED = "unauthorized";


    /**
     * File
     */
    public static final String UPLOAD_FILE_FAILED = "upload_file_failed";
    public static final String FILE_NOT_FOUND = "file_not_found";
    public static final String DELETE_FILE_FAILED = "delete_file_failed";

    /**
     * Authentication
     */
    public static final String API_KEY_IS_REQUIRED = "api_key_is_required";
    public static final String API_KAY_NOT_MATCH = "api_key_not_match";
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String EXPIRED_TOKEN = "expired_token";
    public static final String REVOKED_TOKEN = "revoked_token";
    public static final String INVALID_REFRESH_TOKEN = "invalid_refresh_token";
    public static final String EXPIRED_REFRESH_TOKEN = "expired_refresh_token";
    public static final String REVOKED_REFRESH_TOKEN = "revoked_refresh_token";
    public static final String REFRESH_TOKEN_NOT_FOUND = "refresh_token_not_found";
    public static final String ACCOUNT_IS_ACTIVE = "account_is_active";
    public static final String ACCOUNT_IS_NOT_ACTIVE = "account_is_not_active";


    /**
     * Account
     */
    public static final String ACCOUNT_NOT_FOUND = "account_not_found";
    public static final String INCORRECT_EMAIL_OR_PASSWORD = "incorrect_email_or_password";
    public static final String ACCOUNT_IS_DISABLED = "account_is_disabled";
    public static final String ACCOUNT_ID_IS_REQUIRED = "account_id_is_required";

    /**
     * Thread
     */
    public static final String THREAD_NOT_FOUND = "thread_not_found";
    public static final String THREAD_NOT_AVAILABLE = "thread_not_available";
    public static final String THREAD_REQUEST_INVALID = "thread_request_invalid";
    public static final String THREAD_PARENT_NOT_FOUND = "thread_parent_not_found";
    public static final String THREAD_PARENT_NOT_AVAILABLE = "thread_parent_not_available";
    public static final String PRIVATE_THREAD_CAN_NOT_HAVE_COMMENT = "private_thread_can_not_have_comment";

    private ErrorMessageConstants() {
    }
}
