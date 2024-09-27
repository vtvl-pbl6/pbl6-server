package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.mapper.ThreadMapper;
import com.dut.pbl6_server.repository.fetch_data.ThreadsFetchRepository;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.FollowersRepository;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.service.ThreadService;
import com.dut.pbl6_server.task_executor.service.ContentModerationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("PostService")
@RequiredArgsConstructor
public class ThreadServiceImpl implements ThreadService {
    private final ContentModerationTaskService contentModerationTaskService;
    private final ThreadsRepository threadsRepository;
    private final ThreadsFetchRepository threadsFetchRepository;
    private final AccountsRepository accountsRepository;
    private final FollowersRepository followersRepository;
    private final ThreadMapper threadMapper;

    @Override
    public Object createThread(String text) {
        if (CommonUtils.String.isNotEmptyOrNull(text)) {
            var newThread = threadsRepository.save(
                Thread.builder()
                    .author(accountsRepository.findByEmail("user@gmail.com").orElse(null))
                    .content(text)
                    .build()
            );


            // Moderates the content of the text
            contentModerationTaskService.moderate(newThread, null);

            return "ok";
        }

        return null;
    }

    @Override
    public ThreadResponse getThreadById(Long id) {
        var thread = threadsFetchRepository.findByIdWithRelationship(id).orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND));
        return threadMapper.toResponse(thread);
    }

    @Override
    public DataWithPage<ThreadResponse> getThreadsByAuthorId(Long authorId, Pageable pageable) {
        var page = threadsFetchRepository.findAllByAuthorId(authorId, pageable);
        return new DataWithPage<>(
            page.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }

    @Override
    public DataWithPage<ThreadResponse> getFollowingThreads(Long userId, Pageable pageable) {
        var followingUserIds = followersRepository.findAllByFollowerId(userId).stream().map(f -> f.getUser().getId()).toList();
        var page = threadsFetchRepository.findAllByAuthorIdIn(followingUserIds, pageable);
        return new DataWithPage<>(
            page.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }
}
