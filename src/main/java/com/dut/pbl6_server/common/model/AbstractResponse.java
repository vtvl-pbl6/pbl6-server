package com.dut.pbl6_server.common.model;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSnakeCaseNaming
public class AbstractResponse {
    private Boolean isSuccess;
    private Object data;
    private List<?> errors;
    private Object metadata;

    /**
     * Create successful generic response instance
     *
     * @param data     Data object
     * @param metadata Meta data include paging information
     * @return AbstractResponse
     */
    public static AbstractResponse success(Object data, Object metadata) {
        return AbstractResponse.builder().isSuccess(true).data(data).metadata(metadata).build();
    }

    public static AbstractResponse successWithoutMeta(Object data) {
        return AbstractResponse.builder().isSuccess(true).data(data).build();
    }

    public static AbstractResponse successWithoutMetaAndData() {
        return AbstractResponse.builder().isSuccess(true).build();
    }

    /**
     * Create failed generic response instance
     *
     * @param errors Error objects description
     * @return AbstractResponse
     */
    public static AbstractResponse errors(List<?> errors) {
        return AbstractResponse.builder().isSuccess(false).errors(errors).build();
    }

    public static AbstractResponse error(Object error) {
        return AbstractResponse.builder().isSuccess(false).errors(List.of(error)).build();
    }
}
