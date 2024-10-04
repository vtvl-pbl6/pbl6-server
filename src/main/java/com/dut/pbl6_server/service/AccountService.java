package com.dut.pbl6_server.service;

import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;

public interface AccountService {
    AccountResponse getAccountInfo(Account currentUser);
}
