package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.model.AbstractEntity;
import com.dut.pbl6_server.dto.respone.NotificationResponse;
import com.dut.pbl6_server.entity.Account;

public interface NotificationService {
    /**
     * Create a notification and save it to database if required.
     * Then send it to receiver via WebSocket.
     *
     * @param sender   sender of notification<p>
     *                 If null, it will be considered as an <i><b>admin</b></i> or <i><b>moderate system</b>.</i>
     * @param receiver receiver of notification<p>
     *                 If null, send to all subscribers (Admin or user public destination based on sender role)
     * @param type     type of notification<p>
     *                 It will be used to <i><b>get content</b></i> of notification, and it also indicates whether we need to <i><b>save the notification to the database or not</b></i>.
     * @param object   object of notification
     * @return DTO response of notification
     */
    NotificationResponse sendNotification(Account sender, Account receiver, NotificationType type, AbstractEntity object);
}