package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ThreadsRepository extends JpaRepository<Thread, Long> {
    @Query("SELECT t FROM Thread t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Thread> findById(Long id);
}
