package com.dut.pbl6_server.repository.fetch_data.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WhereFieldOperator {
    LOWER("LOWER("),
    UPPER("UPPER("),
    CONCAT("CONCAT(");

    private final String value;
}
