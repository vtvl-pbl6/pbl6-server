package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT a.role = 'ADMIN' FROM Account a WHERE a.id = :id")
    boolean isAdministrator(Long id);

    boolean existsByDisplayName(String displayName);

    @Query("""
        SELECT a
        FROM Account a
        LEFT JOIN FETCH a.avatarFile
        WHERE
            LOWER(a.displayName) LIKE LOWER(CONCAT('%', :displayName, '%'))
            AND a.role != 'ADMIN'
            AND (a.visibility = 'PUBLIC' OR a.visibility = 'FRIEND_ONLY')
            AND a.deletedAt IS NULL
        """)
    Page<Account> searchByDisplayName(String displayName, Pageable pageable);
}
