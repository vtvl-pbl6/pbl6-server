package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeUser;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.request.ThreadRequest;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("PostController")
@RequestMapping("/api/v1/thread")
@PreAuthorizeUser
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @GetMapping("/{id}")
    public Object getThreadById(
        @CurrentAccount Account account,
        @PathVariable Long id
    ) {
        try {
            return threadService.getThreadById(account, id);
        } catch (NumberFormatException e) {
            throw new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND);
        }
    }

    @GetMapping()
    public Object getThreads(
        @CurrentAccount Account account,
        @RequestParam(name = "author_id", required = false) Long authorId,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
        @RequestParam(name = "order", defaultValue = "desc") String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return authorId != null
            ? threadService.getThreadsByAuthorId(account, authorId, pageRequest) // Get threads by author
            : threadService.getFollowingThreads(account.getId(), pageRequest); // Get following threads
    }

    @PostMapping
    public Object createThread(
        @CurrentAccount Account account,
        @RequestParam(name = "content", required = false) String content,
        @RequestParam(name = "files", required = false) List<MultipartFile> files,
        @RequestParam(name = "parent_id", required = false) Long parentId,
        @RequestParam(name = "visibility", defaultValue = "public") String visibility
    ) {
        // Validate data
        if (CommonUtils.String.isEmptyOrNull(content) && CommonUtils.List.isEmptyOrNull(files))
            throw new BadRequestException(ErrorMessageConstants.THREAD_REQUEST_INVALID);
        var visibilityEnum = CommonUtils.stringToEnum(visibility, Visibility.class);
        return threadService.createThread(account, new ThreadRequest(content, parentId, files, visibilityEnum));
    }

    @PostMapping("/{threadId}/share")
    public Object shareThread(
        @CurrentAccount Account account,
        @PathVariable Long threadId
    ) {
        return threadService.shareThread(account, threadId);
    }

    @PostMapping("/{threadId}/like")
    public Object likeThread(
        @CurrentAccount Account account,
        @PathVariable Long threadId
    ) {
        return threadService.likeThread(account, threadId);
    }
}
