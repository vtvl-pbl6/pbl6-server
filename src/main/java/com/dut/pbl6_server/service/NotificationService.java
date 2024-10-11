package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.model.AbstractEntity;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Account;

public interface NotificationService {
    NotificationResponse sendNotification(Account sender, Account receiver, NotificationType type, AbstractEntity object);
}