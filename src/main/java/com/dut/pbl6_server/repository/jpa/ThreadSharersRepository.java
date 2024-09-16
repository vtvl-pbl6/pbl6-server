package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.ThreadSharer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadSharersRepository extends JpaRepository<ThreadSharer, Long> {
}
