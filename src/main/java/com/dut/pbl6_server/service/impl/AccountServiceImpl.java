package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.mapper.AccountMapper;
import com.dut.pbl6_server.repository.fetch_data.AccountsFetchRepository;
import com.dut.pbl6_server.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("AccountService")
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountsFetchRepository accountsFetchRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountResponse getAccountInfo(Account currentUser) {
        Optional<Account> accountOptional = accountsFetchRepository.findById(currentUser.getId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            return accountMapper.toResponse(account);
        } else {
            throw new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND);
        }
    }

}
