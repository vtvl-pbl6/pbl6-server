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
    private final NotificationsRepository notificationsRepository;
    private final ThreadMapper threadMapper;

    @Override
    public ThreadResponse createThread(Account currentUser, ThreadRequest request) {
        var parentThread = request.getParentId() != null
            ? threadsRepository.findById(request.getParentId()).orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_PARENT_NOT_FOUND))
            : null;
        Thread createdThread = null;
        List<ThreadFile> createdThreadFiles = null;
        Long notificationId = null;
        try {
            // Check parent thread's visibility and status
            if (parentThread != null && !parentThread.getAuthor().getId().equals(currentUser.getId())) {
                switch (parentThread.getVisibility()) {
                    case PRIVATE ->
                        throw new BadRequestException(ErrorMessageConstants.PRIVATE_THREAD_CAN_NOT_HAVE_COMMENT);
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
            createdThread = threadsRepository.save(
                Thread.builder()
                    .author(currentUser)
                    .content(request.getContent())
                    .parentThread(parentThread)
                    .visibility(parentThread != null ? Visibility.PUBLIC : request.getVisibility()) // Comment is always public
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
                createdThreadFiles = threadFilesRepository.saveAll(threadFiles);
                createdThread.setFiles(createdThreadFiles);
            }

            // Moderate the content and files
            if (CommonUtils.String.isNotEmptyOrNull(request.getContent()) || CommonUtils.List.isNotEmptyOrNull(request.getFiles()))
                contentModerationTaskService.moderate(createdThread);

            // Send comment notification to all subscribers
            if (parentThread != null)
                notificationId = notificationService.sendNotification(currentUser, null, NotificationType.COMMENT, createdThread, false, true).getId();

            return threadMapper.toResponseWithoutComments(createdThread);
        } catch (Exception ex) {
            // Rollback without @Transactional
            if (createdThread != null) threadsRepository.delete(createdThread);
            if (CommonUtils.List.isNotEmptyOrNull(createdThreadFiles)) {
                threadFilesRepository.deleteAll(createdThreadFiles);
                cloudinaryService.deleteFiles(createdThreadFiles.stream().map(e -> e.getFile().getId()).toList());
            }
            if (notificationId != null) notificationsRepository.deleteById(notificationId);
            throw ex;
        }
    }

    @Override
    public ThreadResponse updateThread(Account currentUser, ThreadRequest request) {
        var origialThread = threadsRepository.findById(request.getCurrentThreadId())
            .orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND));
        var originalThreadFiles = threadFilesRepository.findAllByThreadId(origialThread.getId());
        origialThread.setFiles(originalThreadFiles);
        try {
            Thread needUpdateThread = origialThread.clone();

            // Check valid visibility
            if (needUpdateThread.getParentThread() != null && request.getVisibility() != null)
                throw new BadRequestException(ErrorMessageConstants.CANNOT_CHANGE_VISIBILITY_OF_COMMENT);

            // Check permission
            if (!currentUser.getId().equals(needUpdateThread.getAuthor().getId()))
                throw new BadRequestException(ErrorMessageConstants.FORBIDDEN_ACTION);

            // Update the thread
            if (CommonUtils.String.isNotEmptyOrNull(request.getContent()))
                needUpdateThread.setContent(request.getContent());
            if (CommonUtils.List.isNotEmptyOrNull(request.getDeleteFileIds())) {
                // Delete thread files
                threadFilesRepository.deleteAllById(needUpdateThread.getFiles().stream()
                    .filter(
                        e -> request.getDeleteFileIds().contains(e.getFile().getId())
                    ).toList()
                    .stream().map(ThreadFile::getId).toList()
                );
                needUpdateThread.setFiles(needUpdateThread.getFiles().stream()
                    .filter(
                        e -> !request.getDeleteFileIds().contains(e.getFile().getId())
                    ).toList()
                );

                // Delete files from cloudinary and database
                cloudinaryService.deleteFiles(request.getDeleteFileIds());
            }
            needUpdateThread.setVisibility(request.getVisibility());
            needUpdateThread.setHosResult(null); // Reset hos result
            needUpdateThread.setStatus(ThreadStatus.CREATING); // Change status to creating to moderate the content and files
            threadsRepository.save(needUpdateThread);


            // Moderate the content and files
            if (CommonUtils.String.isNotEmptyOrNull(request.getContent()) || CommonUtils.List.isNotEmptyOrNull(request.getFiles()))
                contentModerationTaskService.moderate(needUpdateThread);

            // Send notification to public
            notificationService.sendNotification(currentUser, null, NotificationType.EDIT_THREAD, needUpdateThread, false, true);

            return threadMapper.toResponseWithoutComments(needUpdateThread);
        } catch (Exception e) {
            // Rollback without @Transactional
            threadsRepository.save(origialThread);
            threadFilesRepository.saveAll(originalThreadFiles);
            throw e;
        }
    }

    @Override
    public void deleteThread(Account currentUser, Long threadId) {
        var thread = threadsRepository.findById(threadId)
            .orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND));

        // Check permission
        if (!currentUser.getId().equals(thread.getAuthor().getId()))
            throw new BadRequestException(ErrorMessageConstants.FORBIDDEN_ACTION);

        // Delete thread
        thread.setDeletedAt(CommonUtils.DateTime.getCurrentTimestamp());
        threadsRepository.save(thread);
    }

    @Override
    public ThreadResponse getThreadById(Account currentUser, Long threadId) {
        var thread = threadsFetchRepository.findById(threadId)
            .orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.THREAD_NOT_FOUND));

        // Check if the current user is the author of the thread
        if (currentUser.getId().equals(thread.getAuthor().getId()))
            return threadMapper.toResponse(getCommentsForEachThread(List.of(thread), true).getFirst());

        // Check thread's visibility for the current user who is not the author of the thread
        return switch (thread.getVisibility()) {
            case PRIVATE -> throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);
            case FRIEND_ONLY -> {
                // if the current user is not following the author of the thread then the thread is not available
                if (!followersRepository.isFollowing(thread.getAuthor().getId(), currentUser.getId()))
                    throw new BadRequestException(ErrorMessageConstants.THREAD_NOT_AVAILABLE);

                yield threadMapper.toResponse(getCommentsForEachThread(List.of(thread), true).getFirst());
            }
            case PUBLIC -> threadMapper.toResponse(getCommentsForEachThread(List.of(thread), true).getFirst());
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
            getCommentsForEachThread(page.getContent(), true).stream().map(threadMapper::toResponseWithoutComments).toList(),
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
            getCommentsForEachThread(page.getContent(), true).stream().map(threadMapper::toResponseWithoutComments).toList(),
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
            getCommentsForEachThread(page.getContent(), true).stream().map(threadMapper::toResponseWithoutComments).toList(),
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
            getCommentsForEachThread(contents, true).stream().map(threadMapper::toResponseWithoutComments).toList(),
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
        notificationService.sendNotification(currentUser, null, NotificationType.LIKE, thread, false, true);
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
        notificationService.sendNotification(currentUser, null, NotificationType.UNLIKE, thread, false, true);
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
        sharer = threadSharersRepository.save(sharer);

        // Update shared_num in the thread
        thread.setSharedNum(thread.getSharedNum() + 1);
        thread = threadsRepository.save(thread);
        thread.setSharers(List.of(sharer));

        // Send notification to all subscribers
        notificationService.sendNotification(currentUser, null, NotificationType.SHARE, thread, false, true);
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
        notificationService.sendNotification(currentUser, null, NotificationType.UNSHARED, thread, false, true);
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

    private List<Thread> getCommentsForEachThread(List<Thread> threads, boolean isRecursive) {
        var result = new ArrayList<>(threads);
        var allComments = isRecursive
            ? getCommentsForEachThread(threadsFetchRepository.findAllComments(threads.stream().map(Thread::getId).toList()), false)
            : threadsFetchRepository.findAllComments(threads.stream().map(Thread::getId).toList());

        for (Thread thread : result) {
            thread.setComments(allComments.stream().filter(
                    e -> e.getParentThread().getId().equals(thread.getId())
                ).toList()
            );
        }
        return result;
    }
}
