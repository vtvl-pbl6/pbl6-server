package com.dut.pbl6_server.controller.admin;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeAdmin;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController("AdminThreadController")
@RequestMapping("/api/v1/admin/thread")
@PreAuthorizeAdmin
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @PatchMapping("/{id}/lock")
    public Object lockThread(@CurrentAccount Account account, @PathVariable Long id) {
        threadService.lockThread(account, id);
        return null;
    }

    @PatchMapping("/{id}/unlock")
    public Object unloadThread(@CurrentAccount Account account, @PathVariable Long id) {
        threadService.unlockThread(account, id);
        return null;
    }

    @PostMapping("/{id}/moderation/accept")
    public Object acceptRequestModeration(@CurrentAccount Account account, @PathVariable Long id) {
        threadService.acceptRequestModeration(account, id);
        return null;
    }

    @PostMapping("/{id}/moderation/decline")
    public Object denyRequestModeration(@CurrentAccount Account account, @PathVariable Long id) {
        threadService.denyRequestModeration(account, id);
        return null;
    }
}
