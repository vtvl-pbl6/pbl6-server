package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountsFetchRepository extends FetchRepository<Account, Long, FetchBaseRepository<Account>> {
    Page<Account> searchByDisplayName(String displayName, Pageable pageable);
}
