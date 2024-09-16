package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereElement;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereFieldOperator;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereLogical;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

public class FetchBaseRepositoryTest {

    /*
     * TEST CASES FOR SORT CLAUSE
     */
    @Test
    public void getSortClause_ValidSort_ReturnsSortClauseString() {
        Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("age"));
        String prefix = "u";
        String result = FetchBaseRepository.getSortClause(sort, prefix);
        Assertions.assertEquals(" ORDER BY u.name ASC, u.age DESC", result);
    }

    @Test
    public void getSortClause_NullSort_ReturnsEmptyString() {
        Sort sort = null;
        String result = FetchBaseRepository.getSortClause(sort, "u");
        Assertions.assertEquals("", result);
    }

    @Test
    public void fetSortClause_EmptySort_ReturnsEmptyString() {
        Sort sort = Sort.unsorted();
        String result = FetchBaseRepository.getSortClause(sort, "u");
        Assertions.assertEquals("", result);
    }


    @Test
    public void getSortClause_NoPrefix_ReturnsSortClauseString() {
        Sort sort = Sort.by(Sort.Order.asc("name"));
        String result = FetchBaseRepository.getSortClause(sort, null);
        Assertions.assertEquals(" ORDER BY name ASC", result);
    }

    /*
     * TEST CASES FOR WHERE CLAUSE
     */
    @Test
    public void getWhereClause_SingleCondition_ReturnsWhereClauseString() {
        List<WhereElement> whereElements = List.of(new WhereElement("name", "John", WhereOperator.EQUAL));
        String prefix = "u";
        String result = FetchBaseRepository.getWhereClause(whereElements, prefix);
        Assertions.assertEquals(" WHERE u.name = ?1", result);
    }

    @Test
    public void getWhereClause_MultipleConditions_ReturnsWhereClauseString() {
        List<WhereElement> whereElements = Arrays.asList(
            new WhereElement("name", "John", WhereOperator.EQUAL),
            new WhereElement("age", 30, WhereOperator.GREATER_THAN)
        );
        String prefix = "u";
        String result = FetchBaseRepository.getWhereClause(whereElements, prefix);
        Assertions.assertEquals(" WHERE u.name = ?1 AND u.age > ?2", result);
    }

    @Test
    public void getWhereClause_GroupConditions_ReturnsWhereClauseString() {
        List<WhereElement> whereElements = Arrays.asList(
            new WhereElement("name", "John", WhereOperator.EQUAL, null, WhereLogical.AND),
            new WhereElement("age", 25, WhereOperator.LESS_THAN, null, WhereLogical.START_GROUP_WITH_OR),
            new WhereElement("address", "Viet Nam", WhereOperator.LIKE, null, WhereLogical.OR),
            new WhereElement("university", "DUT", WhereOperator.EQUAL, null, WhereLogical.OR),
            new WhereElement("gpa", 3.6, WhereOperator.GREATER_THAN_OR_EQUAL, null, WhereLogical.END_GROUP_WITH_AND),
            new WhereElement("sex", null, WhereOperator.IS_NOT_NULL)
        );
        String prefix = "u";
        String result = FetchBaseRepository.getWhereClause(whereElements, prefix);
        Assertions.assertEquals(" WHERE u.name = ?1 AND (u.age < ?2 OR u.address LIKE ?3 OR u.university = ?4 OR u.gpa >= ?5) AND u.sex IS NOT NULL", result);
    }

    @Test
    public void getWhereClause_FieldOperator_ReturnsWhereClauseString() {
        List<WhereElement> whereElements = List.of(
            new WhereElement("name", "John", WhereOperator.EQUAL, List.of(WhereFieldOperator.UPPER))
        );
        String prefix = "u";
        String result = FetchBaseRepository.getWhereClause(whereElements, prefix);
        Assertions.assertEquals(" WHERE UPPER(u.name) = ?1", result);
    }

    @Test
    public void getWhereClause_MultipleFieldOperators_ReturnsWhereClauseString() {
        List<WhereElement> whereElements = List.of(
            new WhereElement("first_name, last_name", "John".toUpperCase(), WhereOperator.LIKE, List.of(WhereFieldOperator.UPPER, WhereFieldOperator.CONCAT))
        );
        String prefix = "u";
        String result = FetchBaseRepository.getWhereClause(whereElements, prefix);
        Assertions.assertEquals(" WHERE UPPER(CONCAT(u.first_name, u.last_name)) LIKE ?1", result);
    }

    @Test
    public void getWhereClause_NullWhereElements_ReturnsEmptyString() {
        List<WhereElement> whereElements = null;
        String result = FetchBaseRepository.getWhereClause(whereElements, "u");
        Assertions.assertEquals("", result);
    }

    @Test
    public void getWhereClause_EmptyWhereElements_ReturnsEmptyString() {
        List<WhereElement> whereElements = List.of();
        String result = FetchBaseRepository.getWhereClause(whereElements, "u");
        Assertions.assertEquals("", result);
    }

    @Test
    public void getWhereClause_NoPrefix_ReturnsWhereClauseString() {
        List<WhereElement> whereElements = List.of(
            new WhereElement("name", "John", WhereOperator.EQUAL)
        );
        String result = FetchBaseRepository.getWhereClause(whereElements, null);
        Assertions.assertEquals(" WHERE name = ?1", result);
    }
}
