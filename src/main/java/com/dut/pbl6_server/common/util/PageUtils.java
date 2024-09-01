package com.dut.pbl6_server.common.util;

import com.dut.pbl6_server.common.enums.DataSortOrder;
import com.dut.pbl6_server.common.model.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public final class PageUtils {

    /**
     * Handle make page request for query
     *
     * @param sortBy   Sort By Field
     * @param order    Order By Desc Or Asc
     * @param page     Page No
     * @param pageSize Page Size
     * @return Pageable
     */
    public static Pageable makePageRequest(String sortBy, String order, Integer page, Integer pageSize) {
        page = (page == null || page <= 0) ? 1 : page;
        pageSize = pageSize == null ? 15 : (pageSize >= 30) ? 30 : pageSize;
        Sort sort = null;
        if (CommonUtils.String.isNotEmptyOrNull(order) && CommonUtils.String.isNotEmptyOrNull(sortBy)) {
            String sortField = CommonUtils.Naming.convertToCamelCase(sortBy);
            // sort order
            DataSortOrder dataSortOrder = CommonUtils.stringToEnum(order, DataSortOrder.class);
            sort = switch (dataSortOrder) {
                case DESC -> Sort.by(Sort.Direction.DESC, sortField);
                case ASC -> Sort.by(Sort.Direction.ASC, sortField);
            };
        }
        return sort != null ? PageRequest.of(page - 1, pageSize, sort) : PageRequest.of(page - 1, pageSize);
    }

    /**
     * Page info utils
     */
    public static PageInfo makePageInfo(int currentPage, int paging, long totalCount) {
        return PageInfo.builder()
            .currentPage(currentPage)
            .totalPage((int) Math.ceil(totalCount * 1.0 / paging))
            .totalCount(totalCount)
            .build();
    }

    public static PageInfo makePageInfo(Page<?> page) {
        return page.getTotalPages() != 0 ?
            PageInfo.builder()
                .currentPage(page.getNumber() + 1)
                .totalPage(page.getTotalPages())
                .totalCount(page.getTotalElements())
                .build() :
            makePageInfo(page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private PageUtils() {
    }
}
