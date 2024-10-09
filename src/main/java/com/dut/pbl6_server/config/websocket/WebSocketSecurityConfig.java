package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.entity.enums.AccountRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
@SuppressWarnings("deprecation")
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(final MessageSecurityMetadataSourceRegistry messages) {
        // Roles
        final String USER = AccountRole.USER.name();
        final String ADMIN = AccountRole.ADMIN.name();

        messages
            .nullDestMatcher().authenticated()

            // '/public' destinations
            .simpSubscribeDestMatchers("/public/user/**").hasAuthority(USER)
            .simpSubscribeDestMatchers("/public/admin/**").hasAuthority(ADMIN)

            // '/private' destinations
            .simpSubscribeDestMatchers("/private/*/user/**").hasAuthority(USER)
            .simpSubscribeDestMatchers("/private/*/admin/**").hasAuthority(ADMIN)

            // '/app' destinations
            .simpMessageDestMatchers("/app/user/**").hasAuthority(USER)
            .simpMessageDestMatchers("/app/admin/**").hasAuthority(ADMIN)

            // Other destinations
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll()
            .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
