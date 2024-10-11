package com.dut.pbl6_server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationType {
    FOLLOW("follow", true),
    COMMENT("comment", true),
    LIKE("like", false),
    SHARE("share", false);

    private final String value;
    private final boolean saveToDatabase;
}
