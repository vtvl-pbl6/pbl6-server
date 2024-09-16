package com.dut.pbl6_server.repository.fetch_data.base.custom_model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WhereElement {
    private String key;
    private Object value;
    private WhereOperator operator;
    private List<WhereFieldOperator> fieldOperator;
    @Builder.Default
    private WhereLogical logical = WhereLogical.AND;

    public WhereElement(String key, Object value, WhereOperator operator) {
        this.key = key;
        this.value = value;
        this.operator = operator;
        this.fieldOperator = null;
        this.logical = WhereLogical.AND;
    }

    public WhereElement(String key, Object value, WhereOperator operator, List<WhereFieldOperator> fieldOperator) {
        this.key = key;
        this.value = value;
        this.operator = operator;
        this.fieldOperator = fieldOperator;
        this.logical = WhereLogical.AND;
    }
}
