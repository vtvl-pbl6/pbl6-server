package com.dut.pbl6_server.common.model;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonSnakeCaseNaming
public class PageInfo {
    private int currentPage;
    private int nextPage;
    private int previousPage;
    private int totalPage;
    private long totalCount;

    @Builder
    public PageInfo(int currentPage, int totalPage, long totalCount) {
        this.currentPage = currentPage;
        this.nextPage = (currentPage >= totalPage) ? totalPage : currentPage + 1;
        this.previousPage = (currentPage <= 1)
            ? currentPage
            : (currentPage > totalPage)
            ? totalPage
            : currentPage - 1;
        this.totalPage = totalPage;
        this.totalCount = totalCount;
    }
}
