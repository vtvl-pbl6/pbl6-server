package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThreadsFetchRepository extends FetchRepository<Thread, Long, FetchBaseRepository<Thread>> {
    Page<Thread> findAllByAuthorIdInAndVisibilityInAndStatusNotIn(List<Long> authorIds, List<Visibility> visibilities, List<ThreadStatus> statuses, Pageable pageable);

    Page<Thread> findThreadsByIdInAndVisibilityInAndStatusNotIn(List<Long> threadIds, List<Visibility> visibilities, List<ThreadStatus> statuses, Pageable pageable);
}
