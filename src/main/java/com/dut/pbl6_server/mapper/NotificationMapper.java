package com.dut.pbl6_server.mapper;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.SpringMapStructConfig;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(config = SpringMapStructConfig.class, uses = {AccountMapper.class})
public interface NotificationMapper {
    String TO_RESPONSE_NAMED = "notification_to_response";
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Named(TO_RESPONSE_NAMED)
    @Mapping(source = "sender", target = "sender", qualifiedByName = {AccountMapper.TO_NOTIFICATION_USER_RESPONSE_NAMED})
    @Mapping(source = "receiver", target = "receiver", qualifiedByName = {AccountMapper.TO_NOTIFICATION_USER_RESPONSE_NAMED})
    @Mapping(source = "notification", target = "content", qualifiedByName = "getContent")
    NotificationResponse toResponse(Notification notification);

    @Named("getContent")
    default String getContent(Notification notification) {
        return CommonUtils.String.isNotEmptyOrNull(notification.getCustomContent())
            ? notification.getCustomContent()
            : notification.getContent().replaceAll(CommonConstants.I18N_REGEX_PATTERN, "$1");
    }
}
