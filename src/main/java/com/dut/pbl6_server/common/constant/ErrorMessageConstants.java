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
    public static final String FORBIDDEN_ACTION = "forbidden_action";
    public static final String UNAUTHORIZED = "unauthorized";


    /**
     * File
     */
    public static final String UPLOAD_FILE_FAILED = "upload_file_failed";
    public static final String FILE_NOT_FOUND = "file_not_found";
    public static final String DELETE_FILE_FAILED = "delete_file_failed";
    public static final String FILE_SIZE_LIMIT_EXCEEDED = "file_size_limit_exceeded";

    /**
     * Authentication
     */
    public static final String API_KEY_IS_REQUIRED = "api_key_is_required";
    public static final String API_KEY_NOT_MATCH = "api_key_not_match";
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String EXPIRED_TOKEN = "expired_token";
    public static final String REVOKED_TOKEN = "revoked_token";
    public static final String INVALID_REFRESH_TOKEN = "invalid_refresh_token";
    public static final String EXPIRED_REFRESH_TOKEN = "expired_refresh_token";
    public static final String REVOKED_REFRESH_TOKEN = "revoked_refresh_token";
    public static final String REFRESH_TOKEN_NOT_FOUND = "refresh_token_not_found";
    public static final String ACCOUNT_IS_ACTIVE = "account_is_active";
    public static final String ACCOUNT_IS_NOT_ACTIVE = "account_is_not_active";
    public static final String USERNAME_ALREADY_EXISTS = "username_already_exists";
    public static final String EMAIL_ALREADY_EXISTS = "email_already_exists";
    public static final String CONFIRM_PASSWORD_NOT_MATCHING = "confirm_password_not_matching";
    public static final String INCORRECT_OLD_PASSWORD = "incorrect_old_password";

    /**
     * Account
     */
    public static final String ACCOUNT_NOT_FOUND = "account_not_found";
    public static final String INCORRECT_EMAIL_OR_PASSWORD = "incorrect_email_or_password";
    public static final String ACCOUNT_IS_DISABLED = "account_is_disabled";
    public static final String ACCOUNT_ID_IS_REQUIRED = "account_id_is_required";
    public static final String ACCOUNT_IS_NOT_AVAILABLE = "account_is_not_available";
    public static final String CANNOT_FOLLOW_YOURSELF = "cannot_follow_yourself";
    public static final String ALREADY_FOLLOWED = "already_followed";
    public static final String CANNOT_FOLLOW_ADMIN = "cannot_follow_admin";
    public static final String CANNOT_UNFOLLOW_YOURSELF = "cannot_unfollow_yourself";
    public static final String CANNOT_UNFOLLOW_ADMIN = "cannot_unfollow_admin";
    public static final String ALREADY_UNFOLLOWED = "already_unfollowed";
    public static final String NEW_PASSWORD_MUST_BE_DIFFERENT = "new_password_must_be_different";
    public static final String CANNOT_ACTION_ADMIN_ACCOUNT = "cannot_action_admin_account";
    public static final String ACCOUNT_IS_ALREADY_ACTIVE = "already_active";
    public static final String ACCOUNT_IS_ALREADY_INACTIVE = "already_inactive";
    public static final String AVATAR_IS_REQUIRED = "avatar_is_required";

    /**
     * Thread
     */
    public static final String THREAD_NOT_FOUND = "thread_not_found";
    public static final String THREAD_NOT_AVAILABLE = "thread_not_available";
    public static final String THREAD_REQUEST_INVALID = "thread_request_invalid";
    public static final String THREAD_UPDATE_REQUEST_INVALID = "thread_update_request_invalid";
    public static final String THREAD_PARENT_NOT_FOUND = "thread_parent_not_found";
    public static final String THREAD_PARENT_NOT_AVAILABLE = "thread_parent_not_available";
    public static final String PRIVATE_THREAD_CAN_NOT_HAVE_COMMENT = "private_thread_can_not_have_comment";
    public static final String THREAD_LIKED = "thread_liked";
    public static final String THREAD_UNLIKED = "thread_unliked";
    public static final String CANNOT_SHARE_YOUR_OWN_THREAD = "cannot_share_your_own_thread";
    public static final String ALREADY_SHARED = "already_shared";
    public static final String CANNOT_UNSHARED_YOUR_OWN_THREAD = "cannot_unshared_your_own_thread";
    public static final String ALREADY_UNSHARED = "already_unshared";
    public static final String CANNOT_CHANGE_VISIBILITY_OF_COMMENT = "cannot_change_visibility_of_comment";

    /**
     * Repost
     */
    public static final String REPOST_NOT_FOUND = "repost_not_found";

    /**
     * Notification
     */
    public static final String NOTIFICATION_NOT_FOUND = "notification_not_found";
    public static final String NOTIFICATION_CANT_SEND_TO_YOURSELF = "notification_cant_send_to_yourself";

    private ErrorMessageConstants() {
    }
}
