package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.RefreshToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RefreshTokensRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByAccountId(Long accountId);

    @Query("SELECT r FROM RefreshToken r WHERE r.createdAt < current_timestamp ORDER BY r.createdAt ASC")
    List<RefreshToken> findAllExpired(Pageable pageable);
}
