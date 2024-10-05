package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeUser;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("UserController")
@RequestMapping("/api/v1/user")
@PreAuthorizeUser
@RequiredArgsConstructor
public class UserController {
    private final AccountService accountService;

    @GetMapping
    public Object getUserInfo(
        @CurrentAccount Account account
    ) {
        return accountService.getAccountInfo(account);
    }

}
