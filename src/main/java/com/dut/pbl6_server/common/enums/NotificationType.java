package com.dut.pbl6_server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationType {
    // For both real-time and database
    FOLLOW("follow", true),
    COMMENT("comment", true),
    REQUEST_THREAD_MODERATION("request_thread_moderation", true),
    REQUEST_THREAD_MODERATION_FAILED("request_thread_moderation_failed", true),
    REQUEST_THREAD_MODERATION_SUCCESS("request_thread_moderation_success", true),
    SHARE("share", true),
    ACTIVATE_ACCOUNT("activate_account", true),
    DEACTIVATE_ACCOUNT("deactivate_account", true),
    CUSTOM("custom", true),
    LOCK_THREAD("lock_thread", true),
    UNLOCK_THREAD("unlock_thread", true),
    // For real-time only,
    UNFOLLOW("unfollow", false),
    CREATE_THREAD_DONE("create_thread_done", false),
    LIKE("like", false),
    UNLIKE("unlike", false),
    UNSHARED("unshared", false),
    EDIT_THREAD("edit_thread", false);

    private final String value;
    private final boolean saveToDatabase;
}
