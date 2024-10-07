package com.dut.pbl6_server.controller.test_websocket;

import com.dut.pbl6_server.annotation.aspect.SkipHttpResponseWrapper;
import com.dut.pbl6_server.common.enums.WebSocketDestination;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@RestController("test_websocket")
@RequiredArgsConstructor
public class Controller {
    private final WebSocketUtils webSocketUtils;

    @MessageMapping("/message")
    @SkipHttpResponseWrapper
    public Message receiveMessage(@Payload Message message) {
        var user = webSocketUtils.getSubscribers();
        user.forEach(System.out::println);
        webSocketUtils.sendToAllSubscribers(WebSocketDestination.PUBLIC, message);
        return message;
    }
}
