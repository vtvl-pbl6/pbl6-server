package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadsRepository extends JpaRepository<Thread, Long> {
}
