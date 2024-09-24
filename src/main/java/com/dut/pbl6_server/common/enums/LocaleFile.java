package com.dut.pbl6_server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LocaleFile {
    APP("app"),
    ERROR("errors"),
    VALIDATION("validations");

    private final String value;
}