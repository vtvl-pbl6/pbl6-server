package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.UnauthorizedException;
import com.dut.pbl6_server.config.auth.JwtUtils;
import com.dut.pbl6_server.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InBoundChannelInterceptor implements ChannelInterceptor {
    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor != null ? accessor.getCommand() : null;
        if (command != null) {
            if (StompCommand.CONNECT.equals(command)) {
                // Jwt token
                final String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                if (authorizationHeader == null || !authorizationHeader.startsWith(CommonConstants.JWT_TYPE)) {
                    throw new UnauthorizedException(ErrorMessageConstants.UNAUTHORIZED);
                }

                // Get the token from the header
                String token = authorizationHeader.replaceFirst("%s ".formatted(CommonConstants.JWT_TYPE), "");

                // Set websocket principal to accessor
                Account account = jwtUtils.getAccountFromToken(token);
                WebSocketPrincipal principal = new WebSocketPrincipal(account.getDisplayName());
                accessor.setUser(principal);
            }
        }
        return message;
    }
}
