package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.ThreadFile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThreadFilesRepository extends JpaRepository<ThreadFile, Long> {
    List<ThreadFile> findAllByThreadId(Long threadId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE File f
        SET f.nsfwResult = NULL
        WHERE
                f.nsfwResult IS NOT NULL
            AND f.id IN (
                SELECT tf.file.id
                FROM ThreadFile tf
                WHERE tf.thread.id = :threadId
            )
        """)
    void removeNsfwResultForFile(Long threadId);
}
