package com.dut.pbl6_server.repository.fetch_data.impl;

import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.entity.enums.AccountStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.repository.fetch_data.AccountsFetchRepository;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereElement;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereFieldOperator;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereLogical;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountsFetchRepositoryImpl implements AccountsFetchRepository {
    private final FetchBaseRepository<Account> fetchBaseRepository;

    @Override
    public FetchBaseRepository<Account> getRepository() {
        return fetchBaseRepository;
    }

    @Override
    public Page<Account> searchByDisplayName(String displayName, String currentDisplayName, Pageable pageable) {
        return fetchBaseRepository.fetchAllDataWithPagination(
            List.of(
                new WhereElement("displayName", "%" + displayName.toLowerCase() + "%", WhereOperator.LIKE, List.of(WhereFieldOperator.LOWER)),
                new WhereElement("displayName", currentDisplayName, WhereOperator.NOT_EQUAL),
                new WhereElement("role", AccountRole.ADMIN, WhereOperator.NOT_EQUAL),
                new WhereElement("visibility", Visibility.PUBLIC, WhereOperator.EQUAL, null, WhereLogical.START_GROUP_WITH_OR),
                new WhereElement("visibility", Visibility.FRIEND_ONLY, WhereOperator.EQUAL, null, WhereLogical.END_GROUP_WITH_AND),
                new WhereElement("status", AccountStatus.ACTIVE, WhereOperator.EQUAL),
                new WhereElement("deletedAt", null, WhereOperator.IS_NULL)
            ),
            pageable
        );
    }

    @Override
    public List<Account> getUserAccounts() {
        return fetchBaseRepository.fetchAllDataWithoutPagination(
            List.of(new WhereElement("role", AccountRole.USER, WhereOperator.EQUAL)),
            null
        );
    }

    @Override
    public Optional<Account> getByIdAlthoughDeleted(Long id) {
        return fetchBaseRepository.fetchAllDataWithoutPagination(
            List.of(new WhereElement("id", id, WhereOperator.EQUAL))
            , null
        ).stream().findFirst();
    }

    @Override
    public List<Account> getAccountsByIds(List<Long> ids) {
        return fetchBaseRepository.fetchAllDataWithoutPagination(
            List.of(
                new WhereElement("id", ids, WhereOperator.IN),
                new WhereElement("status", AccountStatus.ACTIVE, WhereOperator.EQUAL),
                new WhereElement("deletedAt", null, WhereOperator.IS_NULL)
            ),
            null
        );
    }
}