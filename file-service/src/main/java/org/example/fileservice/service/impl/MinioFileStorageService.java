package org.example.fileservice.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.fileservice.config.MinioConfig;
import org.example.fileservice.exception.FileStorageException;
import org.example.fileservice.model.FileMetadata;
import org.example.fileservice.repository.FileMetadataRepository;
import org.example.fileservice.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {
    private final FileMetadataRepository fileMetadataRepository;
    private final MinioConfig minioConfig;
    private final MinioClient minioClient;

    @Override
    public FileMetadata uploadFile(MultipartFile file, Long uploaderId, Long taskId) {
        try {
            // Validate bucket exists
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());

            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String storageFilename = UUID.randomUUID() + fileExtension;
            String filePath = "uploads/" + storageFilename;

            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // Save metadata
            FileMetadata metadata = FileMetadata.builder()
                    .originalFileName(originalFilename)
                    .storageFileName(storageFilename)
                    .filePath(filePath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploaderId(uploaderId)
                    .taskId(taskId)
                    .uploadDate(LocalDateTime.now())
                    .build();

            return fileMetadataRepository.save(metadata);
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file", e);
        }
    }

    @Override
    @SneakyThrows
    public String generatePresignedUrl(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(metadata.getFilePath())
                            .expiry(minioConfig.getPresignedUrlExpiry())
                            .build());
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }

    @Override
    @SneakyThrows
    public void deleteFile(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(metadata.getFilePath())
                            .build());

            fileMetadataRepository.delete(metadata);
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file", e);
        }
    }

    @Override
    public FileMetadata getFileMetadata(Long fileId) {
        return null;
    }

    @Override
    public byte[] downloadFile(Long fileId) {
        return new byte[0];
    }
}
