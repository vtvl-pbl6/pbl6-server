package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketUtils {
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;

    public List<String> getSubscribers() {
        return simpUserRegistry.getUsers().stream().map(SimpUser::getName).toList();
    }

    public void sendToAllSubscribers(WebSocketDestination destination, Object payload) {
        // Check if destination is valid
        if (!destination.getName().startsWith("/public"))
            throw new BadRequestException("Destination must start with '/public'");

        messagingTemplate.convertAndSend(destination.getName(), payload);
    }

    public void sendToSubscriber(String displayName, WebSocketDestination destination, Object payload) {
        // Check if subscriber exists
        var subscriber = simpUserRegistry.getUser(displayName);
        if (subscriber == null)
            throw new BadRequestException("Subscriber not found");

        // Check if destination is valid
        if (destination.getName().startsWith("/public"))
            throw new BadRequestException("Destination must not start with '/public'");

        messagingTemplate.convertAndSendToUser(displayName, destination.getName(), payload);
    }
}
