package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AccountsFetchRepository extends FetchRepository<Account, Long, FetchBaseRepository<Account>> {
    Page<Account> searchByDisplayName(String displayName, String currentDisplayName, Pageable pageable);

    List<Account> getUserAccounts();

    Optional<Account> getByIdAlthoughDeleted(Long id);

    List<Account> getAccountsByIds(List<Long> ids);
}
