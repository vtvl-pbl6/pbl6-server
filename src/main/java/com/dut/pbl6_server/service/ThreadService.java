package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.dto.request.ThreadRequest;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import com.dut.pbl6_server.entity.Account;
import org.springframework.data.domain.Pageable;

public interface ThreadService {
    ThreadResponse createThread(Account currentUser, ThreadRequest request);

    ThreadResponse getThreadById(Account currentUser, Long threadId);

    DataWithPage<ThreadResponse> getThreadsByAuthorId(Account currentUser, Long authorId, Pageable pageable);

    DataWithPage<ThreadResponse> getFollowingThreads(Long userId, Pageable pageable);

    DataWithPage<ThreadResponse> getThreadSharesByAccount(Account currentUser, Pageable pageable);
}
