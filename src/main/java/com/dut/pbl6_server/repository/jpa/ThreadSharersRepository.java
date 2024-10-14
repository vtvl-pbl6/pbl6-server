package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.ThreadSharer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThreadSharersRepository extends JpaRepository<ThreadSharer, Long> {
    @Query("SELECT t.thread.id FROM ThreadSharer t WHERE t.user.id = :userId")
    List<Long> getListThreadIdByUserId(@Param("userId") Long userId);
}
