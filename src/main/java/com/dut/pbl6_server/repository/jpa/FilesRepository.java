package com.dut.pbl6_server.repository.jpa;

import com.dut.pbl6_server.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FilesRepository extends JpaRepository<File, Long> {
    @Modifying
    @Query("DELETE FROM File f WHERE f.url = :url")
    void deleteByUrl(String url);
}
