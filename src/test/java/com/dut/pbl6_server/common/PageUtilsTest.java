package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.model.PageInfo;
import com.dut.pbl6_server.common.util.PageUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.Collections;


public class PageUtilsTest {
    @Test
    void makePageRequest_ValidParameters_ReturnsPageRequest() {
        // Act
        Pageable pageable = PageUtils.makePageRequest("sortBy", "ASC", 2, 20);

        // Assert
        Assertions.assertNotNull(pageable);
        Assertions.assertEquals(1, pageable.getPageNumber(), "Page number should be 0-based.");
        Assertions.assertEquals(20, pageable.getPageSize(), "Page size should be 20.");
        Assertions.assertEquals(Sort.by(Sort.Direction.ASC, "sortBy"), pageable.getSort(), "Sort should be ascending by camel case field.");
    }

    @Test
    void makePageRequest_InvalidPage_ReturnsDefaultPageRequest() {
        // Act
        Pageable pageable = PageUtils.makePageRequest("sortBy", "ASC", -1, 20);

        // Assert
        Assertions.assertNotNull(pageable);
        Assertions.assertEquals(0, pageable.getPageNumber(), "Page number should default to 0-based.");
        Assertions.assertEquals(20, pageable.getPageSize(), "Page size should be 20.");
    }

    @Test
    void makePageRequest_InvalidPageSize_ReturnsClampedPageSize() {
        // Act
        Pageable pageable = PageUtils.makePageRequest("sortBy", "ASC", 1, 40);

        // Assert
        Assertions.assertNotNull(pageable);
        Assertions.assertEquals(0, pageable.getPageNumber(), "Page number should be 0-based.");
        Assertions.assertEquals(30, pageable.getPageSize(), "Page size should be clamped to 30.");
    }

    @Test
    void makePageRequest_NoSort_ReturnsPageRequestWithoutSort() {
        // Act
        Pageable pageable = PageUtils.makePageRequest(null, null, 1, 20);

        // Assert
        Assertions.assertNotNull(pageable);
        Assertions.assertEquals(0, pageable.getPageNumber(), "Page number should be 0-based.");
        Assertions.assertEquals(20, pageable.getPageSize(), "Page size should be 20.");
        Assertions.assertEquals(Sort.unsorted(), pageable.getSort(), "Sort should be unsorted.");
    }

    @Test
    void makePageInfo_ValidPageInfo_ReturnsCorrectPageInfo() {
        // Act
        PageInfo pageInfo = PageUtils.makePageInfo(1, 10, 101);

        // Assert
        Assertions.assertNotNull(pageInfo);
        Assertions.assertEquals(1, pageInfo.getCurrentPage());
        Assertions.assertEquals(11, pageInfo.getTotalPage());
        Assertions.assertEquals(101, pageInfo.getTotalCount());
        Assertions.assertEquals(2, pageInfo.getNextPage());
        Assertions.assertEquals(1, pageInfo.getPreviousPage());
    }

    @Test
    void makePageInfo_PageObject_ReturnsCorrectPageInfo() {
        // Arrange
        Page<?> page = new PageImpl<Object>(Collections.emptyList(), PageRequest.of(1, 10), 100);

        // Act
        PageInfo pageInfo = PageUtils.makePageInfo(page);

        // Assert
        Assertions.assertNotNull(pageInfo);
        Assertions.assertEquals(2, pageInfo.getCurrentPage());
        Assertions.assertEquals(10, pageInfo.getTotalPage());
        Assertions.assertEquals(100, pageInfo.getTotalCount());
        Assertions.assertEquals(3, pageInfo.getNextPage());
        Assertions.assertEquals(1, pageInfo.getPreviousPage());
    }

    @Test
    void makePageInfo_EmptyPageObject_ReturnsFallbackPageInfo() {
        // Arrange
        Page<?> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        // Act
        PageInfo pageInfo = PageUtils.makePageInfo(page);

        // Assert
        Assertions.assertNotNull(pageInfo);
        Assertions.assertEquals(1, pageInfo.getCurrentPage());
        Assertions.assertEquals(0, pageInfo.getTotalPage());
        Assertions.assertEquals(0, pageInfo.getTotalCount());
        Assertions.assertEquals(0, pageInfo.getNextPage());
        Assertions.assertEquals(1, pageInfo.getPreviousPage());
    }
}
