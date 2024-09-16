package com.dut.pbl6_server.repository.fetch_data.impl;

import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.repository.fetch_data.AccountsFetchRepository;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountsFetchRepositoryImpl implements AccountsFetchRepository {
    private final FetchBaseRepository<Account> fetchBaseRepository;

    @Override
    public FetchBaseRepository<Account> getRepository() {
        return fetchBaseRepository;
    }
}
