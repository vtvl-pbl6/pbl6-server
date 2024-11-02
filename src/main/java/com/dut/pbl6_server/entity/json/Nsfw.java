package com.dut.pbl6_server.entity.json;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Nsfw {
    private String url;
    private double score;
    private String message;

    public static Nsfw fromMap(Map<String, ?> map) {
        return Nsfw.builder()
            .url((String) map.get("url"))
            .score((double) map.get("score"))
            .message((String) map.get("message"))
            .build();
    }
}
