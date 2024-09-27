package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import org.springframework.data.domain.Pageable;

public interface ThreadService {
    Object createThread(String text);

    ThreadResponse getThreadById(Long id);

    DataWithPage<ThreadResponse> getThreadsByAuthorId(Long authorId, Pageable pageable);

    DataWithPage<ThreadResponse> getFollowingThreads(Long userId, Pageable pageable);
}
