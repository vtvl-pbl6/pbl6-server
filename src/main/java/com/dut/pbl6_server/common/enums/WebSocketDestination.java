package com.dut.pbl6_server.common.enums;

import com.dut.pbl6_server.entity.enums.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebSocketDestination {
    //
    // For public
    //
    PUBLIC_USER("/public/user"),
    PUBLIC_ADMIN("/public/admin"),

    //
    // For private
    //
    PRIVATE_USER_NOTIFICATION("/user/notification"),
    PRIVATE_USER_THREAD("/user/thread"),
    PRIVATE_USER_ERROR("/user/error"),

    PRIVATE_ADMIN_NOTIFICATION("/admin/notification"),
    PRIVATE_ADMIN_THREAD("/admin/thread"),
    PRIVATE_ADMIN_ERROR("/admin/error");

    private final String value;

    public static WebSocketDestination getDestination(NotificationType type, AccountRole receiverRole, boolean publicAdminFlag, boolean publicUserFlag) {
        if (publicAdminFlag) return PUBLIC_ADMIN;
        if (publicUserFlag) return PUBLIC_USER;

        return switch (type) {
            case FOLLOW,
                 UNFOLLOW,
                 COMMENT,
                 REQUEST_THREAD_MODERATION,
                 REQUEST_THREAD_MODERATION_FAILED,
                 REQUEST_THREAD_MODERATION_SUCCESS,
                 ACTIVATE_ACCOUNT,
                 DEACTIVATE_ACCOUNT,
                 CUSTOM -> switch (receiverRole) {
                case USER -> PRIVATE_USER_NOTIFICATION;
                case ADMIN -> PRIVATE_ADMIN_NOTIFICATION;
            };
            case LIKE, UNLIKE, SHARE, UNSHARED, CREATE_THREAD_DONE, EDIT_THREAD, LOCK_THREAD, UNLOCK_THREAD ->
                switch (receiverRole) {
                    case USER -> PRIVATE_USER_THREAD;
                    case ADMIN -> PRIVATE_ADMIN_THREAD;
                };
        };
    }
}
