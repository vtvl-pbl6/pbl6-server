package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.UnauthorizedException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.auth.JwtUtils;
import com.dut.pbl6_server.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor != null ? accessor.getCommand() : null;
        if (command != null) {

            // Authenticate for CONNECT command
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
                var authenticatedUser = new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
                accessor.setUser(authenticatedUser);
            }

            // Authenticate for SUBSCRIBE command
            else if (StompCommand.SUBSCRIBE.equals(command)) {
                var authenticatedUser = (UsernamePasswordAuthenticationToken) accessor.getUser();
                var subscribeDestination = accessor.getDestination();

                if (authenticatedUser == null)
                    throw new UnauthorizedException(ErrorMessageConstants.UNAUTHORIZED);

                if (CommonUtils.String.isNotEmptyOrNull(subscribeDestination) && subscribeDestination.startsWith("/private")) {
                    var destination = subscribeDestination.split("/");

                    if (destination.length < 3)
                        throw new UnauthorizedException(ErrorMessageConstants.UNAUTHORIZED);

                    var userEmail = destination[2];
                    if (!((Account) authenticatedUser.getPrincipal()).getEmail().equals(userEmail))
                        throw new UnauthorizedException(ErrorMessageConstants.UNAUTHORIZED);
                }
            }
        }
        return message;
    }
}
