package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.LocaleFile;
import com.dut.pbl6_server.common.enums.LocaleLanguage;
import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.model.AbstractEntity;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.I18nUtils;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Notification;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.mapper.NotificationMapper;
import com.dut.pbl6_server.repository.jpa.NotificationsRepository;
import com.dut.pbl6_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationsRepository notificationsRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketUtils webSocketUtils;

    @Override
    public NotificationResponse sendNotification(Account sender, Account receiver, NotificationType type, AbstractEntity object) {
        // Check if sender and receiver are the same
        if (sender != null && receiver != null && sender.getEmail().equals(receiver.getEmail())) {
            throw new BadRequestException(ErrorMessageConstants.NOTIFICATION_CANT_SEND_TO_YOURSELF);
        }

        // Get content of notification in default language in order to save it to database
        String savedContent = getContent(type, sender, LocaleLanguage.VI.getValue(), object);

        // Get content of notification in receiver's language
        String receiverContent = savedContent;
        if (receiver != null && !receiver.getLanguage().equals(I18nUtils.getCurrentLanguage().getValue())) {
            receiverContent = getContent(type, sender, receiver.getLanguage(), object);
        }

        // Save notification to database if it is required
        var notification = Notification.builder()
            .sender(sender)
            .receiver(receiver)
            .content(savedContent)
            .objectId(object != null ? object.getId() : null)
            .type(type.name())
            .build();
        var newNotification = type.isSaveToDatabase()
            ? notificationsRepository.save(notification)
            : notification;
        newNotification.setContent(receiverContent); // Set content in receiver's language
        var response = notificationMapper.toResponse(newNotification); // Convert to response

        // Send notification (via WebSocket) to specific subscriber if receiver is not null else send to all subscribers
        if (receiver != null)
            webSocketUtils.sendToSubscriber(
                receiver.getEmail(),
                WebSocketDestination.getDestination(type, receiver.getRole(), sender != null ? sender.getRole() : null),
                response
            );
        else
            webSocketUtils.sendToAllSubscribers(WebSocketDestination.PUBLIC_USER, response);
        return response;
    }

    private String getContent(NotificationType type, Account sender, String language, AbstractEntity object) {
        I18nUtils.setLanguage(language);
        try {
            return switch (type) {
                case FOLLOW -> sender != null
                    ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, sender.getDisplayName())
                    : null;
                case COMMENT -> {
                    var comment = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(comment.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, sender.getDisplayName(), comment.getContent())
                        : I18nUtils.tr("notification.comment_file", LocaleFile.APP, sender.getDisplayName());
                }
                case REQUEST_THREAD_MODERATION -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr(String.format("notification.%s.with_content", type.getValue()), LocaleFile.APP, sender.getDisplayName(), thread.getContent())
                        : I18nUtils.tr(String.format("notification.%s.with_file", type.getValue()), LocaleFile.APP, sender.getDisplayName());
                }
                case REQUEST_THREAD_MODERATION_FAILED, REQUEST_THREAD_MODERATION_SUCCESS -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr(String.format("notification.%s.with_content", type.getValue()), LocaleFile.APP, thread.getContent())
                        : I18nUtils.tr(String.format("notification.%s.with_file", type.getValue()), LocaleFile.APP);
                }
                case LIKE, SHARE, CREATE_THREAD_DONE -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
