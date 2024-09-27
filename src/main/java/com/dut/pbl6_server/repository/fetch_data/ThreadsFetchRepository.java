package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThreadsFetchRepository extends FetchRepository<Thread, Long, FetchBaseRepository<Thread>> {
    Page<Thread> findAllByAuthorId(Long authorId, Pageable pageable);

    Page<Thread> findAllByAuthorIdIn(List<Long> authorIds, Pageable pageable);
}
