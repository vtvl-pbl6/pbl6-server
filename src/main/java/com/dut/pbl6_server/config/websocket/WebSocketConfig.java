package com.dut.pbl6_server.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Set the highest precedence to ensure that the interceptor is executed first.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthChannelInterceptorAdapter authChannelInterceptorAdapter;
    private final WebSocketErrorHandler webSocketErrorHandler;

    /**
     * Register the endpoint that the client will use to connect to the WebSocket server.
     * <p>
     * STOMP (Simple Text Oriented Messaging Protocol) is a sub-protocol running on top of WebSocket
     * and defines the format of messages exchanged between the client and server.
     * <p>
     * Why use STOMP? The WebSocket protocol doesn't define any message structure, which is
     * necessary for scenarios like sending messages to all users or to a specific user.
     *
     * @param registry: StompEndpointRegistry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the endpoint that the client will use to connect to the server
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();

        // Set error handler for the SockJS client
        registry.setErrorHandler(webSocketErrorHandler);
    }

    /**
     * Configure the message broker.
     * <p>
     * The message broker is responsible for routing messages between clients.
     * <p>
     * It will be used to send messages back to clients and handle subscriptions.
     *
     * @param config: MessageBrokerRegistry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // (RECEIVE) messages from clients on destinations starting with "/app". It will be routed to @MessageMapping methods.
        config.setApplicationDestinationPrefixes("/app");

        // (SEND) messages to all clients subscribed to "/public".
        // Enable a simple in-memory broker to handle subscriptions to "/public".
        // Can be replaced with RabbitMQ, ActiveMQ, etc. for more advanced setups.
        config.enableSimpleBroker("/public", "/private");

        // (SEND) messages to a specific user on destinations starting with "/private".
        // This is used for private messaging.
        config.setUserDestinationPrefix("/private");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add an interceptor to the input channel to process messages before they are sent to the controller.
        registration.interceptors(authChannelInterceptorAdapter);
    }
}
