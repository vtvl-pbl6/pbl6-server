package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.aspect.SkipHttpResponseWrapper;
import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("NotificationController")
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final WebSocketUtils webSocketUtils;

    @GetMapping
    public Object test() {
        webSocketUtils.sendError("user@gmail.com", "");
        webSocketUtils.sendToAllSubscribers(WebSocketDestination.PUBLIC_USER, "Hello from public");
        webSocketUtils.sendToSubscriber("user@gmail.com", WebSocketDestination.PRIVATE_USER_NOTIFICATION, "Hello from private");
        return null;
    }

    @MessageMapping("/message")
    @SkipHttpResponseWrapper
    public String sendMessage(@Payload String payload) {
        webSocketUtils.sendToAllSubscribers(WebSocketDestination.PUBLIC_USER, payload);
        return payload;
    }
}
