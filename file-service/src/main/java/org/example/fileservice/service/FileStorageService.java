package org.example.fileservice.service;

import org.example.fileservice.model.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

public interface FileStorageService {
    FileMetadata uploadFile(MultipartFile file, Long uploaderId, Long taskId);
    FileMetadata getFileMetadata(Long fileId);
    String generatePresignedUrl(Long fileId);
    void deleteFile(Long fileId);
    byte[] downloadFile(Long fileId);
}
