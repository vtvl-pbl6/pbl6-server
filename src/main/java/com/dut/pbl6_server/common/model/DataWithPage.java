package com.dut.pbl6_server.common.model;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataWithPage<T> {
    private List<T> data;
    private PageInfo pageInfo;
}
