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

    public static WebSocketDestination getDestination(NotificationType type, AccountRole receiverRole) {
        if (receiverRole == null)
            return PUBLIC_USER;

        return switch (type) {
            case FOLLOW, COMMENT -> switch (receiverRole) {
                case USER -> PRIVATE_USER_NOTIFICATION;
                case ADMIN -> PRIVATE_ADMIN_NOTIFICATION;
            };
            case LIKE, SHARE -> switch (receiverRole) {
                case USER -> PRIVATE_USER_THREAD;
                case ADMIN -> PRIVATE_ADMIN_THREAD;
            };
        };
    }
}
