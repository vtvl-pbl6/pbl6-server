package com.dut.pbl6_server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebSocketDestination {
    //
    // For public
    //
    PUBLIC("/public"),
    
    //
    // For private
    //
    USER_NOTIFICATION("/notification"),
    USER_THREAD("/thread");

    private final String name;
}
