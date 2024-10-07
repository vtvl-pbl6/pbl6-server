package com.dut.pbl6_server.common.model;

import com.dut.pbl6_server.annotation.json.JsonDateFormat;
import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@JsonSnakeCaseNaming
public class AbstractDTO {
    private Long id;
    @JsonDateFormat
    private Timestamp createdAt;
    @JsonDateFormat
    private Timestamp updatedAt;
    @JsonDateFormat
    private Timestamp deletedAt;
}
