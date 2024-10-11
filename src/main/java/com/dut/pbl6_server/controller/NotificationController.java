package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.service.NotificationService;
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
    private final NotificationService notificationService;
    private final AccountsRepository accountsRepository;
    private final ThreadsRepository threadsRepository;


    @GetMapping
    public Object test() {
        notificationService.sendNotification(
            accountsRepository.findByEmail("admin@gmail.com").orElse(null),
            accountsRepository.findByEmail("user@gmail.com").orElse(null),
            NotificationType.FOLLOW,
            null
        );
        return null;
    }

    @MessageMapping("/message")
    public String sendMessage(@Payload String payload) {
        webSocketUtils.sendToAllSubscribers(WebSocketDestination.PUBLIC_USER, payload);
        return payload;
    }
}
