package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import com.dut.pbl6_server.repository.fetch_data.base.WhereElement;
import com.dut.pbl6_server.repository.fetch_data.base.WhereOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface FetchRepository<T, ID, K extends FetchBaseRepository<T>> {
    K getRepository();

    default Optional<T> findByIdWithRelationship(ID id, String... relationships) {
        return getRepository().fetchAllDataWithoutPagination(
                List.of(
                    WhereElement.builder()
                        .key("id")
                        .value(id)
                        .operator(WhereOperator.EQUAL)
                        .build()
                ),
                null,
                relationships)
            .stream().findFirst();
    }

    default List<T> findAllWithRelationship(String... relationships) {
        return getRepository().fetchAllDataWithoutPagination(null, null, relationships);
    }

    default Page<T> findAllWithRelationship(Pageable pageable, String... relationships) {
        return getRepository().fetchAllDataWithPagination(null, pageable, relationships);
    }

    default List<T> findAllWithRelationship(Sort sort, String... relationships) {
        return getRepository().fetchAllDataWithoutPagination(null, sort, relationships);
    }
}
