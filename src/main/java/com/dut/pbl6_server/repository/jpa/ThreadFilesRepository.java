package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.ThreadFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThreadFilesRepository extends JpaRepository<ThreadFile, Long> {
    List<ThreadFile> findAllByThreadId(Long threadId);
}
