package com.dut.pbl6_server.entity.json;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HosResult {
    private String word;
    private int start;
    private int end;
    private double score;

    public static HosResult fromMap(Map<String, ?> map) {
        return HosResult.builder()
            .word((String) map.get("word"))
            .start((int) map.get("start"))
            .end((int) map.get("end"))
            .score((double) map.get("score"))
            .build();
    }
}
