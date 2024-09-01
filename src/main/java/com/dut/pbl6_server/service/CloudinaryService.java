package com.dut.pbl6_server.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);

    void deleteFileByUrl(String url);
}
