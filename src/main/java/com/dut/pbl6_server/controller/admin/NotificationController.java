package com.dut.pbl6_server.controller.admin;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeAdmin;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.request.NotificationRequest;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("AdminNotificationController")
@RequestMapping("/api/v1/admin/notification")
@PreAuthorizeAdmin
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public Object getNotifications(
        @CurrentAccount Account account,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
        @RequestParam(name = "order", defaultValue = "desc") String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return notificationService.getNotifications(account, pageRequest);
    }

    @GetMapping("/created")
    public Object getCreatedNotifications(
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
        @RequestParam(name = "order", defaultValue = "desc") String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return notificationService.getCreatedNotifications(pageRequest);
    }

    @PostMapping
    public Object createNotification(@CurrentAccount Account account, @Valid @RequestBody NotificationRequest request) {
        return notificationService.createNotification(account, request, false, false);
    }

    @PatchMapping("/{id}")
    public Object updateContentNotification(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return notificationService.updateNotification(id, body.get("content"));
    }


    @DeleteMapping("/{id}")
    public Object deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return null;
    }
}
