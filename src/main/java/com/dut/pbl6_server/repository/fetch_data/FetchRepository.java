package com.dut.pbl6_server.repository.fetch_data;

import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereElement;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface FetchRepository<T, ID, K extends FetchBaseRepository<T>> {
    K getRepository();

    default Optional<T> findById(ID id) {
        return getRepository().fetchAllDataWithoutPagination(
                List.of(
                    new WhereElement("id", id, WhereOperator.EQUAL),
                    new WhereElement("deletedAt", null, WhereOperator.IS_NULL)
                ),
                null)
            .stream().findFirst();
    }

    default List<T> findAll() {
        return getRepository().fetchAllDataWithoutPagination(null, null);
    }

    default Page<T> findAll(Pageable pageable, String... relationships) {
        return getRepository().fetchAllDataWithPagination(null, pageable);
    }

    default List<T> findAll(Sort sort, String... relationships) {
        return getRepository().fetchAllDataWithoutPagination(null, sort);
    }
}
