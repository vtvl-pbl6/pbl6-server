package com.dut.pbl6_server.common.model;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonSnakeCaseNaming
public class ErrorResponse {
    private String code;
    private String message;
}
