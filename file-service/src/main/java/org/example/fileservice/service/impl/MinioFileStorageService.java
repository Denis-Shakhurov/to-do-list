package org.example.fileservice.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.fileservice.config.MinioConfig;
import org.example.fileservice.exception.FileStorageException;
import org.example.fileservice.model.FileMetadata;
import org.example.fileservice.repository.FileMetadataRepository;
import org.example.fileservice.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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

    @SneakyThrows
    @Override
    public FileMetadata getFileMetadata(Long fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("File ID cannot be null");
        }

        // Получаем метаданные из базы данных с обработкой исключения
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Проверяем существование файла в хранилище
        validateFileExistsInStorage(metadata);

        return metadata;
    }

    private void validateFileExistsInStorage(FileMetadata metadata) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(metadata.getFilePath())
                            .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                // Файл есть в БД, но отсутствует в хранилище
                log.error("File {} exists in DB but missing in storage", metadata.getId());
                throw new FileStorageException("File exists in database but not found in storage", e);
            }
            throw new FileStorageException("Failed to verify file existence in storage", e);
        } catch (Exception e) {
            throw new FileStorageException("Failed to access storage", e);
        }
    }

    @SneakyThrows
    @Override
    public byte[] downloadFile(Long fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("File ID cannot be null");
        }

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Проверяем существование файла перед скачиванием
        validateFileExistsInStorage(metadata);

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(metadata.getFilePath())
                        .build())) {

            // Читаем содержимое файла с прогрессом (логирование)
            return readStreamWithProgress(stream, metadata);
        } catch (Exception e) {
            throw new FileStorageException("Failed to download file content", e);
        }
    }

    private byte[] readStreamWithProgress(InputStream stream, FileMetadata metadata) throws IOException {
        long fileSize = metadata.getFileSize();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; // 8KB buffer
        int bytesRead;
        long totalRead = 0;

        log.info("Starting download of file {} ({} bytes)", metadata.getOriginalFileName(), fileSize);

        while ((bytesRead = stream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            totalRead += bytesRead;

            // Логируем прогресс каждые 10%
            if (fileSize > 0) {
                int progress = (int) ((totalRead * 100) / fileSize);
                if (progress % 10 == 0) {
                    log.debug("Download progress: {}%", progress);
                }
            }
        }

        log.info("File {} downloaded successfully", metadata.getOriginalFileName());
        return outputStream.toByteArray();
    }
}
