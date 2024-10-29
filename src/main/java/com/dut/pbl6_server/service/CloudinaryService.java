package com.dut.pbl6_server.service;

import com.dut.pbl6_server.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {
    File uploadFile(MultipartFile file);

    List<File> uploadFiles(List<MultipartFile> files);

    void deleteFiles(List<Long> ids);

    void deleteFileByUrl(String url);
}
