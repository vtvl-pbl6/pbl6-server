package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeUser;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("PostController")
@RequestMapping("/api/v1/thread")
@PreAuthorizeUser
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @GetMapping("/{id}")
    public Object getThreadById(@PathVariable Long id) {
        try {
            return threadService.getThreadById(id);
        } catch (NumberFormatException e) {
            throw new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND);
        }
    }

    @GetMapping()
    public Object getThreads(
        @CurrentAccount Account account,
        @RequestParam(value = "my_threads", defaultValue = "true") boolean myThreads,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "limit", required = false) Integer limit,
        @RequestParam(value = "sort_by", defaultValue = "created_at") String sortBy,
        @RequestParam(value = "order", defaultValue = "desc") String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return myThreads
            ? threadService.getThreadsByAuthorId(account.getId(), pageRequest) // Get my threads
            : threadService.getFollowingThreads(account.getId(), pageRequest); // Get following threads
    }

    @PostMapping
    public Object createThread(@RequestBody Map<String, ?> body) {
        return threadService.createThread(body.get("text").toString());
    }
}
