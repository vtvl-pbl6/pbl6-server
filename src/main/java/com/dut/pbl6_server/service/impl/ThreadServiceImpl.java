package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.request.ThreadRequest;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.*;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.mapper.ThreadMapper;
import com.dut.pbl6_server.repository.fetch_data.ThreadsFetchRepository;
import com.dut.pbl6_server.repository.jpa.*;
import com.dut.pbl6_server.service.CloudinaryService;
import com.dut.pbl6_server.service.NotificationService;
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
    private final NotificationService notificationService;
    private final ThreadsRepository threadsRepository;
    private final ThreadsFetchRepository threadsFetchRepository;
    private final ThreadFilesRepository threadFilesRepository;
    private final ThreadSharersRepository threadSharersRepository;
    private final ThreadReactUsersRepository threadReactUsersRepository;
    private final FollowersRepository followersRepository;
    private final ThreadMapper threadMapper;

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

        // Send notification to the author of the parent thread
        if (parentThread != null)
            notificationService.sendNotification(currentUser, createdThread.getParentThread().getAuthor(), NotificationType.COMMENT, createdThread);

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
            page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusNotIn(
                List.of(authorId),
                List.of(), // All visibilities
                List.of(), // All statuses
                pageable
            );
        }

        // Check if the current user is following the author
        else if (followersRepository.isFollowing(authorId, currentUser.getId())) {
            page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusNotIn(
                List.of(authorId),
                List.of(Visibility.PUBLIC, Visibility.FRIEND_ONLY),
                List.of(ThreadStatus.PENDING, ThreadStatus.CREATING),
                pageable
            );
        }

        // The current user is visitor
        else
            page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusNotIn(
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

        var page = threadsFetchRepository.findAllByAuthorIdInAndVisibilityInAndStatusNotIn(
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
        List<Long> threadIds = threadSharersRepository.getListThreadIdByUserId(currentUser.getId());

        if (CommonUtils.List.isEmptyOrNull(threadIds))
            throw new NotFoundObjectException(ErrorMessageConstants.REPOST_NOT_FOUND);

        Page<Thread> page = threadsFetchRepository.findThreadsByIdInAndVisibilityInAndStatusNotIn(
            threadIds,
            List.of(), // All visibilities
            List.of(), // All statuses
            pageable
        );

        return new DataWithPage<>(
            page.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }

    @Override
    public DataWithPage<ThreadResponse> getThreadSharesByUserId(Account currentUser, Long userId, Pageable pageable) {
        List<Long> threadIds = threadSharersRepository.getListThreadIdByUserId(userId);
        boolean isFollowing = followersRepository.isFollowing(userId, currentUser.getId());

        if (CommonUtils.List.isEmptyOrNull(threadIds))
            throw new NotFoundObjectException(ErrorMessageConstants.REPOST_NOT_FOUND);

        var page = threadsFetchRepository.findThreadsByIdInAndVisibilityInAndStatusNotIn(
            threadIds,
            List.of(), // All visibilities
            List.of(), // All statuses
            pageable
        );

        // Normalize data
        var contents = page.stream().peek(thread -> {
            boolean hideData = false;
            if (thread.getStatus() == ThreadStatus.CREATING) hideData = true;
            else
                switch (thread.getVisibility()) {
                    case PRIVATE -> hideData = true;
                    case FRIEND_ONLY -> {
                        if (!isFollowing) hideData = true;
                    }
                }

            if (hideData) {
                thread.setContent(null);
                thread.setFiles(null);
                thread.setHosResult(null);
                thread.setReactionNum(0);
                thread.setSharedNum(0);
                thread.setComments(null);
                thread.setSharers(null);
            }
        }).toList();

        return new DataWithPage<>(
            contents.stream().map(threadMapper::toResponse).toList(),
            PageUtils.makePageInfo(page)
        );
    }

    @Override
    @Transactional
    public void likeThread(Account currentUser, Long threadId) {
        var thread = checkThreadVisibility(currentUser, threadId);

        // Save react user to database if not exists else set deleted_at to null
        var reactUser = threadReactUsersRepository.findByThreadIdAndUserId(threadId, currentUser.getId()).orElse(null);
        if (reactUser == null)
            reactUser = ThreadReactUser.builder()
                .thread(thread)
                .user(currentUser)
                .build();
        else if (reactUser.getDeletedAt() != null)
            reactUser.setDeletedAt(null);
        else
            throw new BadRequestException(ErrorMessageConstants.THREAD_LIKED);
        threadReactUsersRepository.save(reactUser);

        // Update reaction_num in the thread
        thread.setReactionNum(thread.getReactionNum() + 1);
        threadsRepository.save(thread);

        // Send notification to all subscribers
        notificationService.sendNotification(null, null, NotificationType.LIKE, thread);
    }

    @Override
    @Transactional
    public void unlikeThread(Account currentUser, Long threadId) {
        var thread = checkThreadVisibility(currentUser, threadId);

        // Set deleted_at to current time if react user exists else throw exception
        var reactUser = threadReactUsersRepository.findByThreadIdAndUserId(threadId, currentUser.getId()).orElse(null);
        if (reactUser != null && reactUser.getDeletedAt() == null)
            reactUser.setDeletedAt(CommonUtils.DateTime.getCurrentTimestamp());
        else
            throw new BadRequestException(ErrorMessageConstants.THREAD_UNLIKED);
        threadReactUsersRepository.save(reactUser);

        // Update reaction_num in the thread
        thread.setReactionNum(thread.getReactionNum() - 1);
        threadsRepository.save(thread);

        // Send notification to all subscribers
        notificationService.sendNotification(null, null, NotificationType.UNLIKE, thread);
    }

    @Override
    @Transactional
    public void shareThread(Account currentUser, Long threadId) {
        var thread = checkThreadVisibility(currentUser, threadId);

        // Check current user is thread's author
        if (currentUser.getId().equals(thread.getAuthor().getId()))
            throw new BadRequestException(ErrorMessageConstants.CANNOT_SHARE_YOUR_OWN_THREAD);

        // Save sharer to database if not exists else throw exception
        var sharer = threadSharersRepository.findByThreadIdAndUserId(threadId, currentUser.getId()).orElse(null);
        if (sharer != null)
            throw new BadRequestException(ErrorMessageConstants.ALREADY_SHARED);
        else
            sharer = new ThreadSharer(thread, currentUser);
        threadSharersRepository.save(sharer);

        // Update shared_num in the thread
        thread.setSharedNum(thread.getSharedNum() + 1);
        threadsRepository.save(thread);

        // Send notification to all subscribers
        notificationService.sendNotification(null, null, NotificationType.SHARE, thread);
    }

    @Override
    @Transactional
    public void unsharedThread(Account currentUser, Long threadId) {
        var thread = checkThreadVisibility(currentUser, threadId);

        // Check current user is thread's author
        if (currentUser.getId().equals(thread.getAuthor().getId()))
            throw new BadRequestException(ErrorMessageConstants.CANNOT_UNSHARED_YOUR_OWN_THREAD);

        // Delete sharer from database if exists else throw exception
        var sharer = threadSharersRepository.findByThreadIdAndUserId(threadId, currentUser.getId()).orElse(null);
        if (sharer == null)
            throw new BadRequestException(ErrorMessageConstants.ALREADY_UNSHARED);
        else
            threadSharersRepository.delete(sharer);

        // Update shared_num in the thread
        thread.setSharedNum(thread.getSharedNum() - 1);
        threadsRepository.save(thread);

        // Send notification to all subscribers
        notificationService.sendNotification(null, null, NotificationType.UNSHARED, thread);
    }

    private Thread checkThreadVisibility(Account currentUser, Long threadId) {
        var thread = threadsRepository.findById(threadId).orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND));
        boolean isAuthor = currentUser.getId().equals(thread.getAuthor().getId());

        // Throw exception if the thread is creating
        if (thread.getStatus() == ThreadStatus.CREATING)
            throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);

        // Check thread's visibility for the current user who is not the author of the thread
        if (!isAuthor)
            switch (thread.getVisibility()) {
                case PRIVATE -> throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);
                case FRIEND_ONLY -> {
                    if (!followersRepository.isFollowing(thread.getAuthor().getId(), currentUser.getId()))
                        throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);
                }
            }
        return thread;
    }
}
