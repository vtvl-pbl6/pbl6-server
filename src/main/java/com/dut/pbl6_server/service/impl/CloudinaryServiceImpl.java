package com.dut.pbl6_server.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.repository.jpa.FilesRepository;
import com.dut.pbl6_server.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    private final FilesRepository filesRepository;

    @Override
    public File uploadFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return filesRepository.save(
                File.builder()
                    .name(file.getOriginalFilename())
                    .size((int) (file.getSize() / 1024)) // KB
                    .url((String) uploadResult.get("url"))
                    .mimeType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessageConstants.UPLOAD_FILE_FAILED);
        }
    }

    @Override
    public List<File> uploadFiles(List<MultipartFile> files) {
        try {
            if (CommonUtils.List.isEmptyOrNull(files)) return List.of();
            List<File> tmp = new ArrayList<>();
            for (MultipartFile file : files) {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                tmp.add(
                    File.builder()
                        .name(file.getOriginalFilename())
                        .size((int) (file.getSize() / 1024)) // KB
                        .url((String) uploadResult.get("url"))
                        .mimeType(file.getContentType())
                        .build()
                );
            }
            return filesRepository.saveAll(tmp);
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessageConstants.UPLOAD_FILE_FAILED);
        }
    }

    @Override
    public void deleteFiles(List<Long> ids) {
        try {
            List<File> files = filesRepository.findAllById(ids);
            for (File file : files) {
                String publicId = extractPublicIdFromUrl(file.getUrl());
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
            filesRepository.deleteAll(files);
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessageConstants.DELETE_FILE_FAILED);
        }
    }

    @Override
    public void deleteFileByUrl(String url) {
        try {
            String publicId = extractPublicIdFromUrl(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            filesRepository.deleteByUrl(url);
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessageConstants.DELETE_FILE_FAILED);
        }
    }

    private String extractPublicIdFromUrl(String url) {
        String[] parts = url.split("/");
        String publicIdWithFormat = parts[parts.length - 1];
        return publicIdWithFormat.substring(0, publicIdWithFormat.lastIndexOf('.'));
    }
}
