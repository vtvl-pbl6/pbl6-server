package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketUtils {
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;

    public List<String> getSubscribedEmails() {
        return simpUserRegistry.getUsers().stream().map(SimpUser::getName).toList();
    }

    public List<Account> getSubscribers() {
        var subscribers = new ArrayList<Account>();
        for (var user : simpUserRegistry.getUsers()) {
            var subscriber = (UsernamePasswordAuthenticationToken) user.getPrincipal();
            if (subscriber != null)
                subscribers.add((Account) subscriber.getPrincipal());
        }
        return subscribers;
    }

    public Optional<Account> getSubscriber(String email) {
        return getSubscribers().stream().filter(account -> account.getEmail().equals(email)).findFirst();
    }


    public void sendToAllSubscribers(WebSocketDestination destination, Object payload) {
        // Check if destination is valid
        if (!destination.getName().startsWith("/public"))
            throw new BadRequestException("[from sendToAllSubscribers] Destination must start with '/public'");

        messagingTemplate.convertAndSend(destination.getName(), payload);
    }

    public void sendToSubscriber(String email, WebSocketDestination destination, Object payload) {
        // Check if subscriber exists
        var subscriber = simpUserRegistry.getUser(email);
        if (subscriber == null)
            throw new BadRequestException("Subscriber not found");

        // Check if destination is valid
        if (destination.getName().startsWith("/public"))
            throw new BadRequestException("[from sendToSubscriber] Destination must not start with '/public'");

        messagingTemplate.convertAndSendToUser(email, destination.getName(), payload);
    }

    public void sendError(String email, Object message) {
        // Check if subscriber exists
        Account subscriber = getSubscriber(email).orElseThrow(() -> new BadRequestException("Subscriber not found"));

        // Get destination
        var destination = switch (subscriber.getRole()) {
            case ADMIN -> WebSocketDestination.PRIVATE_ADMIN_NOTIFICATION;
            case USER -> WebSocketDestination.PRIVATE_USER_NOTIFICATION;
        };

        // Send error message
        messagingTemplate.convertAndSendToUser(email, destination.getName(), message);
    }
}
