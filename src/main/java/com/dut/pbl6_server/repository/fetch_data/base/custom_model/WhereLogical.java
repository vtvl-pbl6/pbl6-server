package com.dut.pbl6_server.repository.fetch_data.base.custom_model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WhereLogical {
    END_GROUP_WITH_AND(") AND"),
    END_GROUP_WITH_OR(") OR"),
    START_GROUP_WITH_AND("AND"),
    START_GROUP_WITH_OR("OR"),
    AND("AND"),
    OR("OR");

    private final String value;

    public boolean isStartGroup() {
        return this == START_GROUP_WITH_AND || this == START_GROUP_WITH_OR;
    }

    public boolean isEndGroup() {
        return this == END_GROUP_WITH_AND || this == END_GROUP_WITH_OR;
    }
}
