package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.ErrorUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    protected Message<byte[]> handleInternal(
        StompHeaderAccessor errorHeaderAccessor,
        byte[] errorPayload,
        Throwable cause,
        StompHeaderAccessor clientHeaderAccessor
    ) {
        if (cause == null) return super.handleInternal(errorHeaderAccessor, errorPayload, null, clientHeaderAccessor);
        errorHeaderAccessor.setMessage(null); // clear message to prevent infinite loop
        if (clientHeaderAccessor.getCommand() != null && clientHeaderAccessor.getCommand() != StompCommand.CONNECT) {
            errorHeaderAccessor.setHeader("stompCommand", clientHeaderAccessor.getCommand());
        }

        // Get error message
        var rootCause = getRootCause(cause);
        String json = switch (rootCause.getClass().getSimpleName()) {
            case "AccessDeniedException" -> CommonUtils.Json.encode(
                ErrorUtils.getExceptionError(ErrorMessageConstants.FORBIDDEN)
            );
            case "AuthenticationCredentialsNotFoundException" -> CommonUtils.Json.encode(
                ErrorUtils.getExceptionError(ErrorMessageConstants.UNAUTHORIZED)
            );
            default -> {
                var errorResponse = ErrorUtils.getExceptionError(rootCause.getMessage());
                yield errorResponse.getCode() == null
                    ? rootCause.getMessage()
                    : CommonUtils.Json.encode(errorResponse);
            }
        };


        // Return message
        return MessageBuilder.createMessage(
            CommonUtils.String.isNotEmptyOrNull(json)
                ? json.getBytes(StandardCharsets.UTF_8)
                : rootCause.getCause().getMessage().getBytes(StandardCharsets.UTF_8),
            errorHeaderAccessor.getMessageHeaders()
        );
    }

    private Throwable getRootCause(Throwable cause) {
        if (cause.getCause() == null) return cause;
        return getRootCause(cause.getCause());
    }
}
