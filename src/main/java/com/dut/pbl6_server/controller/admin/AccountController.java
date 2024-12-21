package com.dut.pbl6_server.controller.admin;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeAdmin;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController("AdminAccountController")
@RequestMapping("/api/v1/admin/account")
@RequiredArgsConstructor
@PreAuthorizeAdmin
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public Object getAll() {
        return accountService.getAccounts();
    }

    @GetMapping("/{id}")
    public Object getAccountInfoById(@PathVariable Long id) {
        return accountService.getAccountInfoByAdmin(id);
    }

    @PatchMapping("/{id}/deactivate")
    public Object deactivateAccount(@CurrentAccount Account account, @PathVariable Long id) {
        return accountService.deactivateAccount(account, id);
    }

    @PatchMapping("/{id}/activate")
    public Object activateAccount(@CurrentAccount Account account, @PathVariable Long id) {
        return accountService.activateAccount(account, id);
    }
}
