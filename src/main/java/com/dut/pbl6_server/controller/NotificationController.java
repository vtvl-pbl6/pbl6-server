package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeAll;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("NotificationController")
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final WebSocketUtils webSocketUtils;
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorizeAll
    public Object getNotifications(
        @CurrentAccount Account currentAccount,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
        @RequestParam(name = "order", defaultValue = "desc") String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return notificationService.getNotifications(currentAccount, pageRequest);
    }

    // Error handling method
    @MessageExceptionHandler
    public String handleError(MessageHandlingException e) {
        return "Error occurred: " + e.getMessage();
    }
}
