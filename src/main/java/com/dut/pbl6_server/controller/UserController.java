package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeAll;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeUser;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.request.UpdateProfileRequest;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("UserController")
@RequestMapping("/api/v1/user")
@PreAuthorizeUser
@RequiredArgsConstructor
public class UserController {
    private final AccountService accountService;

    @GetMapping
    @PreAuthorizeAll
    public Object getUserInfo(@CurrentAccount Account account, @RequestParam(name = "id", required = false) Long id) {
        return id != null
            ? accountService.getAccountInfoById(account, id)
            : accountService.getAccountInfo(account);
    }

    @GetMapping("/{id}/followers")
    public Object getFollowers(
        @CurrentAccount Account account,
        @PathVariable Long id,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", required = false) String sortBy,
        @RequestParam(name = "order", required = false) String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return accountService.getFollowers(account, id, pageRequest);
    }

    @GetMapping("/{id}/following")
    public Object getFollowingUsers(
        @CurrentAccount Account account, @PathVariable Long id,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", required = false) String sortBy,
        @RequestParam(name = "order", required = false) String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return accountService.getFollowingUsers(account, id, pageRequest);
    }


    @GetMapping("/search")
    public Object searchUser(
        @CurrentAccount Account account,
        @RequestParam(name = "display_name", required = true) String displayName,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "sort_by", required = false) String sortBy,
        @RequestParam(name = "order", required = false) String order
    ) {
        var pageRequest = PageUtils.makePageRequest(sortBy, order, page, limit);
        return accountService.searchUser(account, displayName, pageRequest);
    }

    @PostMapping("/{id}/follow")
    public Object followUser(@CurrentAccount Account account, @PathVariable Long id) {
        accountService.followUser(account, id);
        return null;
    }

    @PostMapping("/{id}/unfollow")
    public Object unfollowUser(@CurrentAccount Account account, @PathVariable Long id) {
        accountService.unfollowUser(account, id);
        return null;
    }

    @PatchMapping
    public Object editProfile(@CurrentAccount Account account, @RequestBody UpdateProfileRequest request) {
        return accountService.editProfile(account, request);
    }

    @PatchMapping("/avatar")
    public Object uploadAvatar(@CurrentAccount Account account, @RequestParam(name = "avatar", required = false) MultipartFile avatar) {
        return accountService.uploadAvatar(account, avatar);
    }
}
