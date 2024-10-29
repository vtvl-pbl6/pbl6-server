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
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Notification;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.mapper.NotificationMapper;
import com.dut.pbl6_server.repository.jpa.NotificationsRepository;
import com.dut.pbl6_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String savedContent = getContent(type, sender, LocaleLanguage.VI.getValue(), object, false);

        // Get content of notification in receiver's language
        String receiverContent = savedContent;
        if (receiver != null && !receiver.getLanguage().equals(I18nUtils.getCurrentLanguage().getValue())) {
            receiverContent = getContent(type, sender, receiver.getLanguage(), object, false);
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
        try {
            if (receiver != null)
                webSocketUtils.sendToSubscriber(
                    receiver.getEmail(),
                    WebSocketDestination.getDestination(type, receiver.getRole(), sender != null ? sender.getRole() : null),
                    response
                );
            else
                webSocketUtils.sendToAllSubscribers(
                    WebSocketDestination.getDestination(type, null, sender != null ? sender.getRole() : null),
                    response
                );
        } catch (Exception e) {
            if (receiver != null) webSocketUtils.sendError(receiver.getEmail(), e);
        }

        return response;
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

    private String trContent(Notification notification, String language) {
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
            if (rawMatcher.results().toList().size() == i18nMatcher.results().toList().size()) {
                Map<String, String> i18nMap = new HashMap<>();
                while (rawMatcher.find() && i18nMatcher.find()) {
                    i18nMap.putIfAbsent(rawMatcher.group(), i18nMatcher.group());
                }
                var translatedContent = notification.getContent();
                i18nMap.keySet().forEach(key -> translatedContent.replaceAll(key, i18nMap.get(key)));
                return translatedContent;
            }
        }

        return null;
    }

    private String getContent(NotificationType type, Account sender, String language, AbstractEntity object, boolean forTranslation) {
        I18nUtils.setLanguage(language);
        if (forTranslation)
            return I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP);

        try {
            return switch (type) {
                case FOLLOW -> I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, sender.getDisplayName());
                case REQUEST_THREAD_MODERATION -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, sender.getDisplayName(), ": " + thread.getContent())
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, sender.getDisplayName(), "");
                }
                case COMMENT -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, thread.getAuthor().getDisplayName(), ": " + thread.getContent())
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, thread.getAuthor().getDisplayName(), "");
                }
                case SHARE -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, thread.getSharers().getFirst().getUser().getDisplayName(), ": " + thread.getContent())
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, thread.getSharers().getFirst().getUser().getDisplayName(), "");
                }
                case REQUEST_THREAD_MODERATION_FAILED, REQUEST_THREAD_MODERATION_SUCCESS -> {
                    var thread = (Thread) object;
                    yield CommonUtils.String.isNotEmptyOrNull(thread.getContent())
                        ? I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, ": " + thread.getContent())
                        : I18nUtils.tr("notification." + type.getValue(), LocaleFile.APP, "");
                }
                case LIKE, UNLIKE, UNSHARED, CREATE_THREAD_DONE, UNFOLLOW, EDIT_THREAD -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
