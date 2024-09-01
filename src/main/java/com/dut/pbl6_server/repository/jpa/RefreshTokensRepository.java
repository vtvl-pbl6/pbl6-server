package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokensRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByAccountId(Long accountId);
}
