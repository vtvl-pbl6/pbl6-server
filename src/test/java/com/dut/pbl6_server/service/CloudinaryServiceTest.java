package com.dut.pbl6_server.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.repository.jpa.FilesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class CloudinaryServiceTest {
    /* Mock beans and dependencies */
    @MockBean
    private Cloudinary cloudinary;
    @MockBean
    private FilesRepository filesRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    /* Test data */
    private Uploader mockUploader;
    private List<MultipartFile> invalidFiles; // Not image type
    private List<MultipartFile> validFiles; // Image type
    private List<Long> fileIds;

    @BeforeEach
    public void setUp() {
        mockUploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(mockUploader);
        /* Initialize test data */
        invalidFiles = List.of(
            new MockMultipartFile("invalid1", "invalid1.txt", "text/plain", "invalid1".getBytes()),
            new MockMultipartFile("invalid2", "invalid2.json", "application/json", "invalid2".getBytes())
        );
        validFiles = List.of(
            new MockMultipartFile("valid1", "valid1.png", "image/png", "valid1".getBytes()),
            new MockMultipartFile("valid2", "valid2.jpg", "image/jpeg", "valid2".getBytes())
        );
        fileIds = List.of(1L, 2L, 3L);
    }

    @AfterEach
    public void tearDown() {
        mockUploader = null;
        /* Reset test data */
        invalidFiles = null;
        validFiles = null;
        fileIds = null;
    }

    /* Test cases */
    @Test
    void uploadFile_ValidImage_ReturnsFile() throws Exception {
        // Arrange
        MultipartFile validFile = validFiles.getFirst();
        Map<String, Object> uploadResult = Map.of("url", "https://cloudinary.com/valid1.png");
        when(mockUploader.upload(validFile.getBytes(), ObjectUtils.emptyMap())).thenReturn(uploadResult);

        File savedFile = File.builder()
            .name("valid1.png")
            .size(1) // KB
            .url("https://cloudinary.com/valid1.png")
            .mimeType("image/png")
            .build();
        when(filesRepository.save(any(File.class))).thenReturn(savedFile);

        // Act
        File result = cloudinaryService.uploadFile(validFile);

        // Assert
        assertNotNull(result);
        assertEquals("valid1.png", result.getName());
        assertEquals("https://cloudinary.com/valid1.png", result.getUrl());
        verify(cloudinary.uploader()).upload(validFile.getBytes(), ObjectUtils.emptyMap());
        verify(filesRepository).save(any(File.class));
    }

    @Test
    void uploadFile_InvalidFile_ThrowsBadRequestException() throws IOException {
        // Arrange
        MultipartFile invalidFile = invalidFiles.getFirst();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cloudinaryService.uploadFile(invalidFile));
        assertEquals(ErrorMessageConstants.FILE_TYPE_NOT_SUPPORTED, exception.getMessage());
        verify(cloudinary.uploader(), never()).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadFiles_ValidImages_ReturnsFileList() throws Exception {
        // Arrange
        Map<String, Object> uploadResult1 = Map.of("url", "https://cloudinary.com/valid1.png");
        Map<String, Object> uploadResult2 = Map.of("url", "https://cloudinary.com/valid2.jpg");

        when(mockUploader.upload(validFiles.get(0).getBytes(), ObjectUtils.emptyMap())).thenReturn(uploadResult1);
        when(mockUploader.upload(validFiles.get(1).getBytes(), ObjectUtils.emptyMap())).thenReturn(uploadResult2);

        File file1 = File.builder()
            .name("valid1.png")
            .size(1)
            .url("https://cloudinary.com/valid1.png")
            .mimeType("image/png")
            .build();

        File file2 = File.builder()
            .name("valid2.jpg")
            .size(1)
            .url("https://cloudinary.com/valid2.jpg")
            .mimeType("image/jpeg")
            .build();

        when(filesRepository.saveAll(anyList())).thenReturn(List.of(file1, file2));

        // Act
        List<File> result = cloudinaryService.uploadFiles(validFiles);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cloudinary.uploader(), times(2)).upload(any(byte[].class), anyMap());
        verify(filesRepository).saveAll(anyList());
    }

    @Test
    void deleteFiles_ValidIds_DeletesFromCloudinaryAndRepository() throws Exception {
        // Arrange
        File file1 = File.builder().id(1L).url("https://cloudinary.com/file1.png").build();
        File file2 = File.builder().id(2L).url("https://cloudinary.com/file2.jpg").build();

        when(filesRepository.findAllById(fileIds)).thenReturn(List.of(file1, file2));

        // Act
        cloudinaryService.deleteFiles(fileIds);

        // Assert
        verify(cloudinary.uploader()).destroy("file1", ObjectUtils.emptyMap());
        verify(cloudinary.uploader()).destroy("file2", ObjectUtils.emptyMap());
        verify(filesRepository).deleteAll(List.of(file1, file2));
    }

    @Test
    void deleteFileByUrl_ValidUrl_DeletesFile() throws Exception {
        // Arrange
        String url = "https://cloudinary.com/file1.png";

        // Act
        cloudinaryService.deleteFileByUrl(url);

        // Assert
        verify(cloudinary.uploader()).destroy("file1", ObjectUtils.emptyMap());
        verify(filesRepository).deleteByUrl(url);
    }
}
