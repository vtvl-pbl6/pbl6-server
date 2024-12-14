package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import com.dut.pbl6_server.dto.request.NotificationRequest;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Notification;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.mapper.NotificationMapper;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.NotificationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class NotificationServiceTest {
    /* Mock beans and dependencies */
    @MockBean
    private NotificationsRepository notificationsRepository;
    @MockBean
    private NotificationMapper notificationMapper;
    @MockBean
    private WebSocketUtils webSocketUtils;
    @MockBean
    private AccountsRepository accountsRepository;
    @Autowired
    private NotificationService notificationService;

    /* Test data */
    private Account admin;
    private Account receiver;

    @BeforeEach
    void setUp() {
        // Mock admin and receiver accounts
        admin = Account.builder().id(1L).email("admin@example.com").role(AccountRole.ADMIN).build();
        receiver = Account.builder().id(2L).email("receiver@example.com").role(AccountRole.USER).language("en").build();
    }


    /* Test cases */
    @Test
    void createNotification_ValidRequest_SavesNotificationAndSendsWebSocket() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setReceiverId(receiver.getId());
        request.setContent("Hello");

        Notification notification = Notification.builder().id(1L).content("Hello").build();
        NotificationResponse response = new NotificationResponse();

        when(accountsRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationsRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(response);

        // Act
        NotificationResponse result = notificationService.createNotification(admin, request, false, false);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationsRepository, times(1)).save(notificationCaptor.capture());
        Notification capturedNotification = notificationCaptor.getValue();

        assertThat(capturedNotification.getContent()).isEqualTo("Hello");
        assertThat(capturedNotification.getSender()).isEqualTo(admin);
        assertThat(capturedNotification.getReceiver()).isEqualTo(receiver);

        verify(webSocketUtils, times(1)).sendToSubscriber(
            receiver.getEmail(),
            WebSocketDestination.getDestination(NotificationType.CUSTOM, receiver.getRole(), false, false),
            response
        );
        assertThat(result).isEqualTo(response);
    }

    @Test
    void createNotification_InvalidReceiver_ThrowsBadRequestException() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setReceiverId(99L); // Non-existent receiver ID
        request.setContent("Hello");

        when(accountsRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.createNotification(admin, request, false, false))
            .isInstanceOf(BadRequestException.class)
            .hasMessage(ErrorMessageConstants.ACCOUNT_NOT_FOUND);

        verify(notificationsRepository, never()).save(any());
        verify(webSocketUtils, never()).sendToSubscriber(
            any(),
            any(WebSocketDestination.class),
            any()
        );
    }

    @Test
    void deleteNotification_ValidId_DeletesNotification() {
        // Arrange
        Notification notification = Notification.builder().id(1L).customContent("Custom Content").build();

        when(notificationsRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationsRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.deleteNotification(1L);

        // Assert
        assertThat(notification.getDeletedAt()).isNotNull();
        verify(notificationsRepository, times(1)).save(notification);
    }

    @Test
    void deleteNotification_InvalidId_ThrowsBadRequestException() {
        // Arrange
        when(notificationsRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.deleteNotification(99L))
            .isInstanceOf(BadRequestException.class)
            .hasMessage(ErrorMessageConstants.NOTIFICATION_NOT_FOUND);

        verify(notificationsRepository, never()).save(any());
    }

    @Test
    void sendNotification_SameSenderAndReceiver_DoesNotSaveToDatabase() {
        // Arrange
        receiver = admin; // Sender and receiver are the same
        NotificationType type = NotificationType.FOLLOW;
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(NotificationResponse.builder().build());

        // Act
        NotificationResponse result = notificationService.sendNotification(admin, receiver, type, null, false, false);

        // Assert
        verify(notificationsRepository, never()).save(any());
        verify(webSocketUtils, times(1)).sendToSubscriber(
            receiver.getEmail(),
            WebSocketDestination.getDestination(type, receiver.getRole(), false, false),
            result
        );
        assertThat(result).isNotNull();
    }

    @Test
    void updateNotification_ValidContent_UpdatesSuccessfully() {
        // Arrange
        Notification notification = Notification.builder()
            .id(1L)
            .customContent("Old Content")
            .content("Old Content")
            .build();

        Notification updatedNotification = Notification.builder()
            .id(1L)
            .customContent("New Content")
            .content("New Content")
            .build();

        when(notificationsRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationsRepository.save(any(Notification.class))).thenReturn(updatedNotification);
        when(notificationMapper.toResponse(updatedNotification)).thenReturn(NotificationResponse.builder().content("New Content").build());

        // Act
        NotificationResponse result = notificationService.updateNotification(1L, "New Content");

        // Assert
        assertThat(result.getContent()).isEqualTo("New Content");
        verify(notificationsRepository, times(1)).save(notification);
    }

    @Test
    void updateNotification_EmptyContent_ThrowsBadRequestException() {
        // Arrange
        Notification notification = Notification.builder()
            .id(1L)
            .customContent("Old Content")
            .build();

        when(notificationsRepository.findById(1L)).thenReturn(Optional.of(notification));

        // Act & Assert
        assertThatThrownBy(() -> notificationService.updateNotification(1L, ""))
            .isInstanceOf(BadRequestException.class)
            .hasMessage(ErrorMessageConstants.FORBIDDEN_ACTION);

        verify(notificationsRepository, never()).save(any());
    }
}
