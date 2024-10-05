package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeUser;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("RepostController")
@RequestMapping("/api/v1/repost")
@PreAuthorizeUser
@RequiredArgsConstructor
public class RepostController {
    private final ThreadService threadService;

    @GetMapping()
    public Object getReposts(
        @CurrentAccount Account account,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
        @RequestParam(name = "order", defaultValue = "desc") String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return threadService.getThreadSharesByAccount(account, pageRequest);
    }
}
