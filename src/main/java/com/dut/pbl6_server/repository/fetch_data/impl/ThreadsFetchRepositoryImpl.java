package com.dut.pbl6_server.repository.fetch_data.impl;

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
    public Page<Thread> findAllByAuthorId(Long authorId, Pageable pageable) {
        return fetchBaseRepository.fetchAllDataWithPagination(
            List.of(
                WhereElement.builder().key("author.id").value(authorId).operator(WhereOperator.EQUAL).build(),
                WhereElement.builder().key("status").value(ThreadStatus.PENDING).operator(WhereOperator.NOT_EQUAL).build()
            ),
            pageable
        );
    }

    @Override
    public Page<Thread> findAllByAuthorIdAndVisibilityIn(Long authorId, List<Visibility> visibilities, Pageable pageable) {
        return fetchBaseRepository.fetchAllDataWithPagination(
            List.of(
                WhereElement.builder().key("author.id").value(authorId).operator(WhereOperator.EQUAL).build(),
                WhereElement.builder().key("status").value(ThreadStatus.PENDING).operator(WhereOperator.NOT_EQUAL).build(),
                WhereElement.builder().key("visibility").value(visibilities).operator(WhereOperator.IN).build()
            ),
            pageable
        );
    }

    @Override
    public Page<Thread> findAllByAuthorIdIn(List<Long> authorIds, Pageable pageable) {
        return fetchBaseRepository.fetchAllDataWithPagination(
            List.of(
                WhereElement.builder().key("author.id").value(authorIds).operator(WhereOperator.IN).build(),
                WhereElement.builder().key("status").value(ThreadStatus.PENDING).operator(WhereOperator.NOT_EQUAL).build()
            ),
            pageable
        );
    }

    @Override
    public Page<Thread> findAllByAuthorIdInAndVisibilityIn(List<Long> authorIds, List<Visibility> visibilities, Pageable pageable) {
        return fetchBaseRepository.fetchAllDataWithPagination(
            List.of(
                WhereElement.builder().key("author.id").value(authorIds).operator(WhereOperator.IN).build(),
                WhereElement.builder().key("status").value(ThreadStatus.PENDING).operator(WhereOperator.NOT_EQUAL).build(),
                WhereElement.builder().key("visibility").value(visibilities).operator(WhereOperator.IN).build()
            ),
            pageable
        );
    }
}
