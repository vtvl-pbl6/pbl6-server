package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.ThreadReactUser;
import com.dut.pbl6_server.entity.ThreadSharer;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.repository.fetch_data.ThreadsFetchRepository;
import com.dut.pbl6_server.repository.jpa.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class ThreadServiceTest {
    @MockBean
    private ThreadsRepository threadsRepository;
    @MockBean
    private ThreadReactUsersRepository threadReactUsersRepository;
    @MockBean
    private ThreadSharersRepository threadSharersRepository;
    @MockBean
    private FollowersRepository followersRepository;
    @MockBean
    private ThreadsFetchRepository threadsFetchRepository;
    @MockBean
    private ThreadFilesRepository threadFilesRepository;
    @MockBean
    private NotificationsRepository notificationsRepository;
    @MockBean
    private NotificationService notificationService;
    @Autowired
    private ThreadService threadService;

    @Test
    void likeThread_ValidThread_IncreasesReactionCount() {
        // Arrange
        Long threadId = 1L;
        Account currentUser = new Account();
        currentUser.setId(1L);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(Account.builder().id(2L).build());
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.CREATE_DONE);
        thread.setReactionNum(0);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadReactUsersRepository.findByThreadIdAndUserId(threadId, currentUser.getId())).thenReturn(Optional.empty());

        // Act
        threadService.likeThread(currentUser, threadId);

        // Assert
        Assertions.assertEquals(1, thread.getReactionNum());
        verify(threadsRepository, times(1)).save(thread);
        verify(notificationService, times(1)).sendNotification(eq(currentUser), any(), eq(NotificationType.LIKE), eq(thread), anyBoolean(), anyBoolean());
    }

    @Test
    void likeThread_AlreadyLiked_ThrowsBadRequestException() {
        // Arrange
        Long threadId = 1L;
        Account currentUser = new Account();
        currentUser.setId(1L);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(Account.builder().id(2L).build());
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.CREATE_DONE);
        thread.setReactionNum(0);

        ThreadReactUser reactUser = new ThreadReactUser();
        reactUser.setDeletedAt(null);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadReactUsersRepository.findByThreadIdAndUserId(threadId, currentUser.getId())).thenReturn(Optional.of(reactUser));

        // Act & Assert
        Assertions.assertThrows(BadRequestException.class, () -> threadService.likeThread(currentUser, threadId));
    }

    @Test
    void unlikeThread_ValidThread_DecreasesReactionCount() {
        // Arrange
        Long threadId = 1L;
        Account currentUser = new Account();
        currentUser.setId(1L);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(Account.builder().id(2L).build());
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.CREATE_DONE);
        thread.setReactionNum(1);

        ThreadReactUser reactUser = new ThreadReactUser();
        reactUser.setDeletedAt(null);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadReactUsersRepository.findByThreadIdAndUserId(threadId, currentUser.getId())).thenReturn(Optional.of(reactUser));

        // Act
        threadService.unlikeThread(currentUser, threadId);

        // Assert
        Assertions.assertEquals(0, thread.getReactionNum());
        Assertions.assertNotNull(reactUser.getDeletedAt());
        verify(threadsRepository, times(1)).save(thread);
        verify(threadReactUsersRepository, times(1)).save(reactUser);
        verify(notificationService, times(1)).sendNotification(eq(currentUser), any(), eq(NotificationType.UNLIKE), eq(thread), anyBoolean(), anyBoolean());
    }

    @Test
    void unlikeThread_NotLiked_ThrowsBadRequestException() {
        // Arrange
        Long threadId = 1L;
        Account currentUser = new Account();
        currentUser.setId(1L);

        Thread thread = new Thread();
        thread.setAuthor(Account.builder().id(2L).build());
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.CREATE_DONE);
        thread.setId(threadId);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadReactUsersRepository.findByThreadIdAndUserId(threadId, currentUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(BadRequestException.class, () -> threadService.unlikeThread(currentUser, threadId));
    }

    @Test
    void shareThread_ValidThread_IncreasesSharedCount() {
        // Arrange
        Long threadId = 1L;
        Account currentUser = new Account();
        currentUser.setId(2L);

        Account author = new Account();
        author.setId(1L);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setSharedNum(0);
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.CREATE_DONE);
        thread.setAuthor(author);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadsRepository.save(any(Thread.class))).thenReturn(thread);
        when(threadSharersRepository.findByThreadIdAndUserId(threadId, currentUser.getId())).thenReturn(Optional.empty());
        when(threadSharersRepository.save(any(ThreadSharer.class))).thenReturn(new ThreadSharer());

        // Act
        threadService.shareThread(currentUser, threadId);

        // Assert
        Assertions.assertEquals(1, thread.getSharedNum());
        verify(threadsRepository, times(1)).save(thread);
        verify(threadSharersRepository, times(1)).save(any());
        verify(notificationService, times(1)).sendNotification(eq(currentUser), eq(author), eq(NotificationType.SHARE), eq(thread), anyBoolean(), anyBoolean());
    }

    @Test
    void shareThread_OwnThread_ThrowsBadRequestException() {
        // Arrange
        Long threadId = 1L;
        Account currentUser = new Account();
        currentUser.setId(1L);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(currentUser);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));

        // Act & Assert
        Assertions.assertThrows(BadRequestException.class, () -> threadService.shareThread(currentUser, threadId));
    }

    @Test
    void lockThread_ValidThread_UpdatesStatusToPending() {
        // Arrange
        Long threadId = 1L;
        Account admin = new Account();

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(Account.builder().id(2L).build());
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.CREATE_DONE);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadsRepository.save(thread)).thenReturn(thread);

        // Act
        threadService.lockThread(admin, threadId);

        // Assert
        Assertions.assertEquals(ThreadStatus.PENDING, thread.getStatus());
        verify(threadsRepository, times(1)).save(thread);
        verify(notificationService, times(1)).sendNotification(eq(admin), eq(thread.getAuthor()), eq(NotificationType.LOCK_THREAD), eq(thread), anyBoolean(), anyBoolean());
    }

    @Test
    void unlockThread_ValidThread_UpdatesStatusToCreateDone() {
        // Arrange
        Long threadId = 1L;
        Account admin = new Account();

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(Account.builder().id(2L).build());
        thread.setVisibility(Visibility.PUBLIC);
        thread.setStatus(ThreadStatus.PENDING);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(threadsRepository.save(thread)).thenReturn(thread);

        // Act
        threadService.unlockThread(admin, threadId);

        // Assert
        Assertions.assertEquals(ThreadStatus.CREATE_DONE, thread.getStatus());
        verify(threadsRepository, times(1)).save(thread);
        verify(notificationService, times(1)).sendNotification(eq(admin), eq(thread.getAuthor()), eq(NotificationType.UNLOCK_THREAD), eq(thread), anyBoolean(), anyBoolean());
    }

    @Test
    void requestThreadModeration_ValidRequest_SendsNotification() {
        // Arrange
        Long threadId = 1L;
        String reason = "Inappropriate content";
        Account currentUser = new Account();
        currentUser.setId(1L);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setAuthor(currentUser);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));
        when(notificationsRepository.isAlreadyRequestModeration(currentUser.getId(), threadId)).thenReturn(false);

        // Act
        threadService.requestThreadModeration(currentUser, threadId, reason);

        // Assert
        verify(threadsRepository, times(1)).save(thread);
        verify(notificationService, times(1)).sendNotification(eq(currentUser), any(), eq(NotificationType.REQUEST_THREAD_MODERATION), eq(thread), anyBoolean(), anyBoolean(), eq(reason));
    }

    @Test
    void acceptRequestModeration_ValidRequest_UpdatesStatusToCreateDone() {
        // Arrange
        Long threadId = 1L;
        Account admin = new Account();
        admin.setRole(AccountRole.ADMIN);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setStatus(ThreadStatus.PENDING);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));

        // Act
        threadService.acceptRequestModeration(admin, threadId);

        // Assert
        Assertions.assertEquals(ThreadStatus.CREATE_DONE, thread.getStatus());
        verify(threadsRepository, times(1)).save(thread);
        verify(notificationService, times(1)).sendNotification(eq(admin), eq(thread.getAuthor()), eq(NotificationType.REQUEST_THREAD_MODERATION_SUCCESS), eq(thread), anyBoolean(), anyBoolean());
    }

    @Test
    void denyRequestModeration_ValidRequest_UpdatesStatusToRejected() {
        // Arrange
        Long threadId = 1L;
        Account admin = new Account();
        admin.setRole(AccountRole.ADMIN);

        Thread thread = new Thread();
        thread.setId(threadId);
        thread.setStatus(ThreadStatus.PENDING);

        when(threadsRepository.findById(threadId)).thenReturn(Optional.of(thread));

        // Act
        threadService.denyRequestModeration(admin, threadId);

        // Assert
        Assertions.assertEquals(ThreadStatus.REJECTED, thread.getStatus());
        verify(threadsRepository, times(1)).save(thread);
        verify(notificationService, times(1)).sendNotification(eq(admin), eq(thread.getAuthor()), eq(NotificationType.REQUEST_THREAD_MODERATION_FAILED), eq(thread), anyBoolean(), anyBoolean());
    }
}
