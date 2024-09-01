package com.dut.pbl6_server.repository.fetch_data.base;

import com.dut.pbl6_server.common.util.CommonUtils;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

public interface FetchBaseRepository<T> {
    Class<T> getEntityClass();

    String getEntityName();

    EntityManager getEntityManager();

    List<T> fetchAllDataWithoutPagination(List<WhereElement> whereElements, Sort sort, String... relationships);

    Page<T> fetchAllDataWithPagination(List<WhereElement> whereElements, Pageable pageable, String... relationships);

    default Long countData(List<WhereElement> whereElements) {
        String countResultHql = String.format("SELECT COUNT(tmp) FROM %s tmp %s", getEntityName(), getWhereClause(whereElements, "tmp"));
        var countResultQuery = getEntityManager().createQuery(countResultHql, Long.class);
        if (whereElements != null)
            for (int i = 0; i < whereElements.size(); i++)
                countResultQuery.setParameter(i + 1, whereElements.get(i).getValue());

        return countResultQuery.getSingleResult();
    }

    default List<T> fetchAllDataWithBaseQuery(List<WhereElement> whereElements, String baseQuery, String prefix, Sort sort, Pageable pageable) {
        String whereClause = getWhereClause(whereElements, prefix);
        // sort clause
        String sortClause = (sort == null && pageable != null) ? getSortClause(pageable.getSort(), prefix) : getSortClause(sort, prefix);

        var query = getEntityManager().createQuery(baseQuery + whereClause + sortClause, getEntityClass());

        // set parameters
        if (whereElements != null) {
            int count = 1;
            for (WhereElement e : whereElements)
                if (e.getOperator().isNotNeedParamType())
                    query.setParameter(count++, e.getValue());
        }

        // set pageable
        if (pageable != null)
            query
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

    static String getSortClause(Sort sort, String prefix) {
        if (sort == null || sort.isEmpty() || sort.isUnsorted()) return "";
        return " ORDER BY " + sort.stream().map(
            order -> (CommonUtils.String.isNotEmptyOrNull(prefix) ? String.format("%s.%s", prefix, order.getProperty()) : order.getProperty())
                + " " + order.getDirection()
        ).reduce((a, b) -> a + ", " + b).orElse("");
    }

    static String getWhereClause(List<WhereElement> whereElements, String prefix) {
        if (whereElements == null || whereElements.isEmpty()) return "";
        final String tmp = CommonUtils.String.isNotEmptyOrNull(prefix) ? prefix + "." : "";
        StringBuilder clause = new StringBuilder();
        int count = 1;
        for (int i = 0; i < whereElements.size(); i++) {
            var currentElement = whereElements.get(i);
            // handle key
            StringBuilder key = new StringBuilder();
            if (CommonUtils.List.isNotEmptyOrNull(currentElement.getFieldOperator()))
                key.append(currentElement.getFieldOperator().stream().map(WhereFieldOperator::getValue).reduce((a, b) -> a + b).orElse(""));
            Arrays.stream(currentElement.getKey().split(",")).map(k -> tmp + k.trim()).forEach(k -> key.append(k).append(", ")); // add key with prefix
            key.deleteCharAt(key.length() - 1); // remove last space
            key.deleteCharAt(key.length() - 1); // remove last comma
            key.append(")".repeat(currentElement.getFieldOperator() != null ? currentElement.getFieldOperator().size() : 0)); // close field operator

            // handle operator
            if (currentElement.getOperator().isNotNeedParamType())
                key.append(" ").append(currentElement.getOperator().getValue());
            else
                key.append(" ").append(currentElement.getOperator().getValue()).append(" ?").append(count++);

            // handle logical
            if (i < whereElements.size() - 1) {
                if (currentElement.getLogical().isStartGroup()) {
                    key.insert(0, "(");
                    key.append(" ").append(currentElement.getLogical().getValue());
                } else if (currentElement.getLogical().isEndGroup())
                    key.append(currentElement.getLogical().getValue());
                else
                    key.append(" ").append(currentElement.getLogical().getValue());
            } else if (currentElement.getLogical().isEndGroup()) {
                key.append(")");
            }
            clause.append(key).append(" ");
        }

        return " WHERE " + clause.toString().trim();
    }
}
