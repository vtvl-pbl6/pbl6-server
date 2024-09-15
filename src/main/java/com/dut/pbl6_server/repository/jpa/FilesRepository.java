package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilesRepository extends JpaRepository<File, Long> {
}
