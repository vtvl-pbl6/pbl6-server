package com.dut.pbl6_server.common.constant;

import com.dut.pbl6_server.common.util.CommonUtils;

public final class RedisCacheConstants {

    /*
     * Key storing in Redis
     */
    public static final String AUTH_KEY = "Authentication";

    /*
     * Hash key storing in Redis
     */
    public static String REVOKE_ACCESS_TOKEN_HASH(Long id, boolean isGenerate) {
        if (!isGenerate)
            return "revoke_access_token:%s".formatted(id.toString());
        return "revoke_access_token:%s:%s".formatted(id.toString(), getCurrentTimestamp());
    }

    public static String REVOKE_REFRESH_TOKEN_HASH(Long id, boolean isGenerate) {
        if (!isGenerate)
            return "revoke_refresh_token:%s".formatted(id.toString());
        return "revoke_refresh_token:%s:%s".formatted(id.toString(), getCurrentTimestamp());
    }

    private static String getCurrentTimestamp() {
        return CommonUtils.DateTime.getCurrentTimestamp().toString().replaceAll(" ", "_");
    }


    private RedisCacheConstants() {
    }
}
