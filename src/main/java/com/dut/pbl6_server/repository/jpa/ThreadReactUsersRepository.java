package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.ThreadReactUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThreadReactUsersRepository extends JpaRepository<ThreadReactUser, Long> {
    Optional<ThreadReactUser> findByThreadIdAndUserId(Long threadId, Long userId);
}
