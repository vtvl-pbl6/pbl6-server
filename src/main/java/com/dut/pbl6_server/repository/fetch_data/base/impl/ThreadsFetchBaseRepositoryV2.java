package com.dut.pbl6_server.repository.fetch_data.base.impl;

import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.ThreadFile;
import com.dut.pbl6_server.entity.ThreadReactUser;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import com.dut.pbl6_server.repository.fetch_data.base.custom_model.WhereElement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ThreadsFetchBaseRepositoryV2")
@RequiredArgsConstructor
public class ThreadsFetchBaseRepositoryV2 implements FetchBaseRepository<Thread> {
    @PersistenceContext
    private EntityManager em;


    @Override
    public Class<Thread> getEntityClass() {
        return Thread.class;
    }

    @Override
    public String getEntityName() {
        return "Thread";
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public Object fetchAllDataImplementation(List<WhereElement> whereElements, Sort sort, Pageable pageable) {
        // Base query with 'sharers' relation
        List<Thread> values = fetchAllDataWithBaseQuery(
            whereElements,
            """
                SELECT t
                FROM Thread t
                LEFT JOIN FETCH t.author ta
                LEFT JOIN FETCH ta.avatarFile
                LEFT JOIN FETCH t.parentThread tp
                LEFT JOIN FETCH tp.author tpa
                LEFT JOIN FETCH tpa.avatarFile
                LEFT JOIN FETCH t.sharers ts
                LEFT JOIN FETCH ts.user tsu
                LEFT JOIN FETCH tsu.avatarFile
                """,
            "t",
            sort,
            pageable
        );

        // Fetch 'files' relation
        var files = em.createQuery(
                """
                    SELECT tf
                    FROM ThreadFile tf
                    LEFT JOIN FETCH tf.file
                    WHERE tf.thread IN :values
                    """,
                ThreadFile.class)
            .setParameter("values", values)
            .getResultList();

        values = values.stream().peek( // Set 'files' for each thread
            v -> v.setFiles(
                files.stream().filter(
                    tmp -> tmp.getThread().getId().equals(v.getId())
                ).toList()
            )
        ).toList();

        // Fetch 'react users' relation for threads
        var reactUsers = em.createQuery(
                """
                    SELECT tru
                    FROM ThreadReactUser tru
                    LEFT JOIN FETCH tru.user
                    LEFT JOIN FETCH tru.user.avatarFile
                    WHERE
                        tru.thread IN :threads
                        AND tru.deletedAt IS NULL
                    """,
                ThreadReactUser.class)
            .setParameter("threads", values)
            .getResultList();

        values = values.stream().peek( // Set 'react users' for each thread
            v -> v.setReactUsers(
                reactUsers.stream().filter(
                    tmp -> tmp.getThread().getId().equals(v.getId())
                ).toList()
            )
        ).toList();

        return (pageable != null && pageable.isPaged()) ?
            new PageImpl<>(values, pageable, countData(whereElements)) :
            values;
    }
}
