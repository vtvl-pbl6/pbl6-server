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

@Component("ThreadsFetchBaseRepositoryV1")
@RequiredArgsConstructor
@Deprecated
public class ThreadsFetchBaseRepositoryV1 implements FetchBaseRepository<Thread> {
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

    /**
     * With data in 'parentThread' relation and 'comments' relation, only fetch data in 1st level
     *
     * @deprecated This method is deprecated and will be replaced by {@link ThreadsFetchBaseRepositoryV2#fetchAllDataImplementation(List, Sort, Pageable)}
     */
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

        // Fetch 'comments' relation (1st level)
        var comments = em.createQuery(
                """
                    SELECT t
                    FROM Thread t
                    LEFT JOIN FETCH t.author ta
                    LEFT JOIN FETCH ta.avatarFile
                    LEFT JOIN FETCH t.files
                    WHERE t.parentThread IN :values
                    """,
                Thread.class)
            .setParameter("values", values)
            .getResultList();

        // Fetch 'react users' relation for threads and comments
        var reactUsers = em.createQuery(
                """
                    SELECT tru
                    FROM ThreadReactUser tru
                    LEFT JOIN FETCH tru.user
                    LEFT JOIN FETCH tru.user.avatarFile
                    WHERE
                        (tru.thread IN :threads OR tru.thread IN :comments)
                        AND tru.deletedAt IS NULL
                    """,
                ThreadReactUser.class)
            .setParameter("threads", values)
            .setParameter("comments", comments)
            .getResultList();

        var finalComments = comments.stream().peek( // Set 'react users' for each comment
            v -> v.setReactUsers(
                reactUsers.stream().filter(
                    tmp -> tmp.getThread().getId().equals(v.getId())
                ).toList()
            )
        ).toList();

        values = values.stream().peek( // Set 'react users' for each thread
            v -> v.setReactUsers(
                reactUsers.stream().filter(
                    tmp -> tmp.getThread().getId().equals(v.getId())
                ).toList()
            )
        ).toList();

        values = values.stream().peek( // Set 'comments' for each thread
            v -> v.setComments(
                finalComments.stream().filter(
                    tmp -> tmp.getParentThread().getId().equals(v.getId())
                ).toList()
            )
        ).toList();

        return (pageable != null && pageable.isPaged()) ?
            new PageImpl<>(values, pageable, countData(whereElements)) :
            values;
    }
}
