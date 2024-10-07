package com.dut.pbl6_server.config.websocket;

import com.dut.pbl6_server.common.util.CommonUtils;
import lombok.*;

import java.security.Principal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketPrincipal implements Principal {
    private String displayName;

    @Override
    public String getName() {
        return this.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        return !CommonUtils.String.isEmptyOrNull(this.displayName) && super.equals(obj);
    }
}
