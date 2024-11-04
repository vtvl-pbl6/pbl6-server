package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.LocaleFile;
import com.dut.pbl6_server.common.enums.LocaleLanguage;
import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.model.AbstractEntity;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.I18nUtils;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import com.dut.pbl6_server.dto.request.NotificationRequest;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Notification;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.mapper.NotificationMapper;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.NotificationsRepository;
import com.dut.pbl6_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationsRepository notificationsRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketUtils webSocketUtils;
    private final AccountsRepository accountsRepository;

    @Override
    public NotificationResponse sendNotification(
        Account sender,
        Account receiver,
        NotificationType type,
        AbstractEntity object,
        boolean publicAdminFlag,
        boolean publicUserFlag,
        String... args
    ) {
        // Check if sender and receiver are the same
        if (sender != null && receiver != null && sender.getEmail().equals(receiver.getEmail())) {
            throw new BadRequestException(ErrorMessageConstants.NOTIFICATION_CANT_SEND_TO_YOURSELF);
        }

        // Get content of notification in default language in order to save it to database
        String savedContent = getContent(type, sender, LocaleLanguage.VI.getValue(), object, false, args);

        // Get content of notification in receiver's language
        String receiverContent = savedContent;
        if (receiver != null && !receiver.getLanguage().equals(I18nUtils.getCurrentLanguage().getValue())) {
            receiverContent = getContent(type, sender, receiver.getLanguage(), object, false, args);
        }

        // Save notification to database if it is required
        var notification = Notification.builder()
            .sender(sender)
            .receiver(receiver)
            .content(savedContent)
            .publicAdminFlag(publicAdminFlag)
            .publicUserFlag(publicUserFlag)
            .objectId(object != null ? object.getId() : null)
            .type(type.name())
            .build();
        var newNotification = type.isSaveToDatabase()
            ? notificationsRepository.save(notification)
            : notification;
        newNotification.setContent(receiverContent); // Set content in receiver's language
        var response = notificationMapper.toResponse(newNotification); // Convert to response

        // Send notification (via WebSocket) to specific subscriber if receiver is not null else send to all subscribers
        try {
            if (receiver != null)
                webSocketUtils.sendToSubscriber(
                    receiver.getEmail(),
                    WebSocketDestination.getDestination(type, receiver.getRole(), publicAdminFlag, publicUserFlag),
                    response
                );
            else
                webSocketUtils.sendToAllSubscribers(
                    WebSocketDestination.getDestination(type, null, publicAdminFlag, publicUserFlag),
                    response
                );
        } catch (Exception e) {
            if (receiver != null) webSocketUtils.sendError(receiver.getEmail(), e);
        }

        return response;
    }

    @Override
    public NotificationResponse createNotification(Account admin, NotificationRequest request, boolean publicAdminFlag, boolean publicUserFlag) {
        var receiver = accountsRepository.findById(request.getReceiverId()).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        // Check if sender and receiver are the same
        if (admin.getId().equals(receiver.getId()))
            throw new BadRequestException(ErrorMessageConstants.NOTIFICATION_CANT_SEND_TO_YOURSELF);

        var notification = Notification.builder()
            .sender(admin)
            .receiver(receiver)
            .content(request.getContent())
            .customContent(request.getContent())
            .publicAdminFlag(publicAdminFlag)
            .publicUserFlag(publicUserFlag)
            .type(NotificationType.CUSTOM.name())
            .build();
        notification = notificationsRepository.save(notification);

        // Send notification (via WebSocket) to specific subscriber
        try {
            webSocketUtils.sendToSubscriber(
                receiver.getEmail(),
                WebSocketDestination.getDestination(NotificationType.CUSTOM, receiver.getRole(), publicAdminFlag, publicUserFlag),
                notificationMapper.toResponse(notification));
        } catch (Exception e) {
            webSocketUtils.sendError(receiver.getEmail(), e);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse updateNotification(Long notificationId, String content) {
        var notification = notificationsRepository.findById(notificationId).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.NOTIFICATION_NOT_FOUND));

        if (notification.getCustomContent() == null || CommonUtils.String.isEmptyOrNull(content))
            throw new BadRequestException(ErrorMessageConstants.FORBIDDEN_ACTION);

        notification.setContent(content);
        notification.setCustomContent(content);
        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }

    @Override
    public void deleteNotification(Long notificationId) {
        var notification = notificationsRepository.findById(notificationId).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.NOTIFICATION_NOT_FOUND));

        if (notification.getCustomContent() == null)
            throw new BadRequestException(ErrorMessageConstants.FORBIDDEN_ACTION);

        notification.setDeletedAt(CommonUtils.DateTime.getCurrentTimestamp());
        notificationsRepository.save(notification);
    }

    @Override
    public DataWithPage<NotificationResponse> getNotifications(Account account, Pageable pageable) {
        var page = notificationsRepository.getNotificationsByReceiverId(account.getId(), account.getRole().name(), pageable);
        return DataWithPage.<NotificationResponse>builder()
            .data(page.getContent().stream().map(notification -> {
                // Set content in receiver's language if it is not in default language
                if (!account.getLanguage().equals(I18nUtils.DEFAULT_LANGUAGE.getValue())) {
                    var content = trContent(notification, account.getLanguage());
                    if (CommonUtils.String.isNotEmptyOrNull(content))
                        notification.setContent(content);
                }
                return notificationMapper.toResponse(notification);
            }).toList())
            .pageInfo(PageUtils.makePageInfo(page))
            .build();
    }

    @Override
    public DataWithPage<NotificationResponse> getCreatedNotifications(Pageable pageable) {
        var page = notificationsRepository.getCreatedNotifications(pageable);
        return DataWithPage.<NotificationResponse>builder()
            .data(page.getContent().stream().map(notificationMapper::toResponse).toList())
            .pageInfo(PageUtils.makePageInfo(page))
            .build();
    }

    private String trContent(Notification notification, String language) {
        if (CommonUtils.String.isEmptyOrNull(notification.getContent())) return null;
        // Get content for translation
        var i18nContent = getContent(
            CommonUtils.stringToEnum(notification.getType(), NotificationType.class),
            notification.getSender(),
            language,
            null,
            true
        );

        // Replace content which starts with <i18n> tag
        if (CommonUtils.String.isNotEmptyOrNull(i18nContent)) {
            Pattern pattern = Pattern.compile(CommonConstants.I18N_REGEX_PATTERN);
            Matcher rawMatcher = pattern.matcher(notification.getContent());
            Matcher i18nMatcher = pattern.matcher(i18nContent);
            var rawMatcherResults = rawMatcher.results().toList();
            var i18nMatcherResults = i18nMatcher.results().toList();
            if (rawMatcherResults.size() == i18nMatcherResults.size()) {
                Map<String, String> i18nMap = new HashMap<>();
                for (int i = 0; i < rawMatcherResults.size(); i++) {
                    i18nMap.putIfAbsent(rawMatcherResults.get(i).group(), i18nMatcherResults.get(i).group());
                }
                var translatedContent = notification.getContent();
                for (String key : i18nMap.keySet()) {
                    translatedContent = translatedContent.replaceAll(key, i18nMap.get(key));
                }
                return translatedContent;
            }
        }

        return null;
    }

    private String getContent(NotificationType type, Account sender, String language, AbstractEntity object, boolean forTranslation, String... args) {
        I18nUtils.setLanguage(language);
        if (forTranslation)
            return I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP);

        try {
            return switch (type) {
                case FOLLOW -> I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, sender.getDisplayName());
                case REQUEST_THREAD_MODERATION, COMMENT -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, concatenateParams(args, sender.getDisplayName(), ": " + thread.getContent()))
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, concatenateParams(args, sender.getDisplayName(), ""));
                }
                case SHARE -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, concatenateParams(args, thread.getSharers().getFirst().getUser().getDisplayName(), ": " + thread.getContent()))
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, concatenateParams(args, thread.getSharers().getFirst().getUser().getDisplayName(), ""));
                }
                case ACTIVATE_ACCOUNT, DEACTIVATE_ACCOUNT ->
                    I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP);
                case REQUEST_THREAD_MODERATION_FAILED, REQUEST_THREAD_MODERATION_SUCCESS, LOCK_THREAD,
                     UNLOCK_THREAD -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, concatenateParams(args, ": " + thread.getContent()))
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, concatenateParams(args, ""));
                }
                case LIKE, UNLIKE, UNSHARED, CREATE_THREAD_DONE, UNFOLLOW, EDIT_THREAD, CUSTOM -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method to concatenate parameters
    private String[] concatenateParams(String[] args, String... elements) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, elements);
        Collections.addAll(list, args);
        return list.toArray(new String[0]);
    }
}
