package com.dut.pbl6_server.config.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventListener
    public void handleConnectListener(SessionConnectedEvent event) {
        logger.info("[ws-connected] socket connect :{}", event.getUser() != null ? event.getUser().getName() : event.getMessage());
    }

    @EventListener
    public void handleDisconnectListener(SessionDisconnectEvent event) {
        logger.info("[ws-disconnected] socket disconnect :{}", event.getUser() != null ? event.getUser().getName() : event.getMessage());
    }
}
