package com.dut.pbl6_server.repository.fetch_data.base.impl;

import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Follower;
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

@Component
@RequiredArgsConstructor
public class AccountsFetchBaseRepository implements FetchBaseRepository<Account> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Class<Account> getEntityClass() {
        return Account.class;
    }

    @Override
    public String getEntityName() {
        return "Account";
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public Object fetchAllDataImplementation(List<WhereElement> whereElements, Sort sort, Pageable pageable) {
        // Base query with 'followers' relation
        List<Account> values = fetchAllDataWithBaseQuery(
            whereElements,
            """
                SELECT a
                FROM Account a
                LEFT JOIN FETCH a.avatarFile
                LEFT JOIN FETCH a.followers af
                LEFT JOIN FETCH af.user.avatarFile
                LEFT JOIN FETCH af.follower aff
                LEFT JOIN FETCH aff.avatarFile
                """,
            "a",
            sort,
            pageable
        );

        // Fetch 'followingUsers' relation
        var followingUsers = em.createQuery(
                """
                    SELECT fu
                    FROM Follower fu
                    LEFT JOIN FETCH fu.follower fuf
                    LEFT JOIN FETCH fuf.avatarFile
                    LEFT JOIN FETCH fu.user fuu
                    LEFT JOIN FETCH fuu.avatarFile
                    WHERE fu.follower IN :values
                    """,
                Follower.class)
            .setParameter("values", values)
            .getResultList();

        values = values.stream().peek(
            v -> v.setFollowingUsers(
                followingUsers.stream().filter(
                    tmp -> tmp.getFollower().getId().equals(v.getId())
                ).toList()
            )
        ).toList();

        return (pageable != null && pageable.isPaged()) ?
            new PageImpl<>(values, pageable, countData(whereElements)) :
            values;
    }
}
