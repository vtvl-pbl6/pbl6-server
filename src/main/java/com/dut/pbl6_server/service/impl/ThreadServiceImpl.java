package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.request.ThreadRequest;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.ThreadFile;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.mapper.ThreadMapper;
import com.dut.pbl6_server.repository.fetch_data.ThreadsFetchRepository;
import com.dut.pbl6_server.repository.jpa.FollowersRepository;
import com.dut.pbl6_server.repository.jpa.ThreadFilesRepository;
import com.dut.pbl6_server.repository.jpa.ThreadSharersRepository;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.service.CloudinaryService;
import com.dut.pbl6_server.service.ThreadService;
import com.dut.pbl6_server.task_executor.service.ContentModerationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("PostService")
@RequiredArgsConstructor
public class ThreadServiceImpl implements ThreadService {
    private final ContentModerationTaskService contentModerationTaskService;
    private final CloudinaryService cloudinaryService;
    private final ThreadFilesRepository threadFilesRepository;
    private final ThreadsRepository threadsRepository;
    private final ThreadsFetchRepository threadsFetchRepository;
    private final FollowersRepository followersRepository;
    private final ThreadMapper threadMapper;
    private final ThreadSharersRepository threadSharersRepository;

    @Override
    @Transactional
    public ThreadResponse createThread(Account currentUser, ThreadRequest request) {
        var parentThread = request.getParentId() != null
            ? threadsRepository.findById(request.getParentId()).orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_PARENT_NOT_FOUND))
            : null;

        // Check parent thread's visibility and status
        if (parentThread != null) {
            switch (parentThread.getVisibility()) {
                case PRIVATE -> throw new BadRequestException(
                    currentUser.getId().equals(parentThread.getAuthor().getId())
                        ? ErrorMessageConstants.THREAD_PARENT_NOT_AVAILABLE
                        : ErrorMessageConstants.PRIVATE_THREAD_CAN_NOT_HAVE_COMMENT
                );
                case FRIEND_ONLY -> {
                    // if the current user is not following the author of the parent thread then the parent thread is not available
                    if (!followersRepository.isFollowing(parentThread.getAuthor().getId(), currentUser.getId()))
                        throw new BadRequestException(ErrorMessageConstants.THREAD_PARENT_NOT_AVAILABLE);
                }
            }

            if (parentThread.getStatus() == ThreadStatus.CREATING || parentThread.getStatus() == ThreadStatus.PENDING)
                throw new BadRequestException(ErrorMessageConstants.THREAD_PARENT_NOT_AVAILABLE);
        }

        // Save a new thread
        var createdThread = threadsRepository.save(
            Thread.builder()
                .author(currentUser)
                .content(request.getContent())
                .parentThread(parentThread)
                .visibility(request.getVisibility())
                .build()
        );

        // Save the files of the thread
        if (CommonUtils.List.isNotEmptyOrNull(request.getFiles())) {
            var uploadedFiles = cloudinaryService.uploadFiles(request.getFiles());
            var threadFiles = new ArrayList<ThreadFile>();
            for (var file : uploadedFiles) {
                threadFiles.add(
                    ThreadFile.builder()
                        .thread(createdThread)
                        .file(file)
                        .build()
                );
            }
            var createdThreadFiles = threadFilesRepository.saveAll(threadFiles);
            createdThread.setFiles(createdThreadFiles);
        }

        // Moderate the content and files
        if (CommonUtils.String.isNotEmptyOrNull(request.getContent()))
            contentModerationTaskService.moderate(createdThread);

        return threadMapper.toResponse(createdThread);
    }

    @Override
    public ThreadResponse getThreadById(Account currentUser, Long threadId) {
        var thread = threadsFetchRepository.findById(threadId)
            .orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND));

        // Check if the current user is the author of the thread
        if (currentUser.getId().equals(thread.getAuthor().getId()))
            return threadMapper.toResponse(thread);

        // Check thread's visibility for the current user who is not the author of the thread
        return switch (thread.getVisibility()) {
            case PRIVATE -> throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);
            case FRIEND_ONLY -> {
                // if the current user is not following the author of the thread then the thread is not available
                if (!followersRepository.isFollowing(thread.getAuthor().getId(), currentUser.getId()))
                    throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);

                yield threadMapper.toResponse(thread);
            }
            case PUBLIC -> threadMapper.toResponse(thread);
        };
    }

    @Override
    public DataWithPage<ThreadResponse> getThreadsByAuthorId(Account currentUser, Long authorId, Pageable pageable) {
        Page<Thread> page = null;

        // Check if the current user is the author
        if (currentUser.getId().equals(authorId)) {
            page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusesNotIn(
                List.of(authorId),
                List.of(), // All visibilities
                List.of(), // All statuses
                pageable
            );
        }

        // Check if the current user is following the author
        else if (followersRepository.isFollowing(authorId, currentUser.getId())) {
            page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusesNotIn(
                List.of(authorId),
                List.of(Visibility.PUBLIC, Visibility.FRIEND_ONLY),
                List.of(ThreadStatus.PENDING, ThreadStatus.CREATING),
                pageable
            );
        }

        // The current user is visitor
        else
            page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusesNotIn(
                List.of(authorId),
                List.of(Visibility.PUBLIC),
                List.of(ThreadStatus.PENDING, ThreadStatus.CREATING),
                pageable
            );

        return new DataWithPage<>(
            page.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }

    @Override
    public DataWithPage<ThreadResponse> getFollowingThreads(Long userId, Pageable pageable) {
        var followingUserIds = followersRepository.findAllByFollowerId(userId).stream().map(f -> f.getUser().getId()).toList();

        // If the current user is not following anyone
        if (CommonUtils.List.isEmptyOrNull(followingUserIds))
            return new DataWithPage<>(List.of(), PageUtils.makePageInfo(Page.empty()));

        var page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusesNotIn(
            followingUserIds,
            List.of(Visibility.PUBLIC, Visibility.FRIEND_ONLY),
            List.of(ThreadStatus.PENDING, ThreadStatus.CREATING),
            pageable
        );
        return new DataWithPage<>(
            page.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }

    @Override
    public DataWithPage<ThreadResponse> getThreadSharesByAccount(Account currentUser, Pageable pageable) {
        Page<Thread> page = null;

        List<Long> threadIds = threadSharersRepository.getListThreadIdByUser(currentUser);

        if (CommonUtils.List.isEmptyOrNull(threadIds)) {
            throw new NotFoundObjectException(ErrorMessageConstants.REPOST_NOT_FOUND);
        }

        page = threadsFetchRepository.findThreadsByThreadIds(threadIds, pageable);

        return new DataWithPage<>(
            page.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }

}
