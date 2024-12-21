package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.dto.request.ThreadRequest;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import com.dut.pbl6_server.entity.Account;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThreadService {
    ThreadResponse createThread(Account currentUser, ThreadRequest request);

    ThreadResponse updateThread(Account currentUser, ThreadRequest request);

    void deleteThread(Account currentUser, Long threadId);

    ThreadResponse getThreadById(Account currentUser, Long threadId);

    DataWithPage<ThreadResponse> getThreadsByAuthorId(Account currentUser, Long authorId, Pageable pageable);

    DataWithPage<ThreadResponse> getFollowingThreads(Long userId, Pageable pageable);

    DataWithPage<ThreadResponse> getThreadSharesByAccount(Account currentUser, Pageable pageable);

    DataWithPage<ThreadResponse> getThreadSharesByUserId(Account currentUser, Long userId, Pageable pageable);

    void likeThread(Account currentUser, Long threadId);

    void unlikeThread(Account currentUser, Long threadId);

    void shareThread(Account currentUser, Long threadId);

    void unsharedThread(Account currentUser, Long threadId);

    void lockThread(Account admin, Long threadId);

    void unlockThread(Account admin, Long threadId);

    NotificationResponse requestThreadModeration(Account currentUser, Long threadId, String reason);

    void acceptRequestModeration(Account currentUser, Long threadId);

    void denyRequestModeration(Account currentUser, Long threadId);

    List<ThreadResponse> getRequestModerateThreads();

    ThreadResponse getRequestModerateThreadById(Long threadId);
}
