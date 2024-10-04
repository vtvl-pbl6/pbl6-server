package com.dut.pbl6_server.repository.fetch_data.impl;

import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.repository.fetch_data.ThreadsFetchRepository;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereElement;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ThreadsFetchRepositoryImpl implements ThreadsFetchRepository {
    private final FetchBaseRepository<Thread> fetchBaseRepository;

    @Override
    public FetchBaseRepository<Thread> getRepository() {
        return fetchBaseRepository;
    }

    @Override
    public Page<Thread> findAllByAuthorIdInAndVisibilityInAndStatusesNotIn(
        List<Long> authorIds,
        List<Visibility> visibilities,
        List<ThreadStatus> statuses,
        Pageable pageable
    ) {
        var whereElements = new ArrayList<WhereElement>();

        if (CommonUtils.List.isNotEmptyOrNull(authorIds))
            whereElements.add(WhereElement.builder().key("author.id").value(authorIds).operator(WhereOperator.IN).build());

        if (CommonUtils.List.isNotEmptyOrNull(visibilities))
            whereElements.add(WhereElement.builder().key("visibility").value(visibilities).operator(WhereOperator.IN).build());

        if (CommonUtils.List.isNotEmptyOrNull(statuses))
            whereElements.add(WhereElement.builder().key("status").value(statuses).operator(WhereOperator.NOT_IN).build());

        return fetchBaseRepository.fetchAllDataWithPagination(CommonUtils.List.isEmptyOrNull(whereElements) ? null : whereElements, pageable);
    }

    @Override
    public Page<Thread> findThreadsByThreadIds(List<Long> threadIds, Pageable pageable) {
        var whereElements = new ArrayList<WhereElement>();
        whereElements.add(WhereElement.builder().key("id").value(threadIds).operator(WhereOperator.IN).build());
        return fetchBaseRepository.fetchAllDataWithPagination(whereElements, pageable);
    }

}
