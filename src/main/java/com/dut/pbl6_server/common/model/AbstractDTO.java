package com.dut.pbl6_server.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AbstractDTO {
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
