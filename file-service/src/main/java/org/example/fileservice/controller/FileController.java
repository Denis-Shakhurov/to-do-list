package org.example.fileservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.fileservice.dto.FileResponse;
import org.example.fileservice.model.FileMetadata;
import org.example.fileservice.security.JwtService;
import org.example.fileservice.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskId", required = false) Long taskId,
            HttpServletRequest request) {
        String jwt = jwtService.resolveToken(request);
        Long userId = Long.parseLong(jwtService.extractUserId(jwt));

        FileMetadata metadata = fileStorageService.uploadFile(file, userId, taskId);
        return ResponseEntity.ok(mapToFileResponse(metadata));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getFileMetadata(@PathVariable Long fileId) {
        FileMetadata metadata = fileStorageService.getFileMetadata(fileId);
        return ResponseEntity.ok(mapToFileResponse(metadata));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<String> generateDownloadUrl(@PathVariable Long fileId) {
        String downloadUrl = fileStorageService.generatePresignedUrl(fileId);
        return ResponseEntity.ok(downloadUrl);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    private FileResponse mapToFileResponse(FileMetadata metadata) {
        return FileResponse.builder()
                .id(metadata.getId())
                .originalName(metadata.getOriginalFileName())
                .contentType(metadata.getContentType())
                .size(metadata.getFileSize())
                .uploadDate(metadata.getUploadDate())
                .taskId(metadata.getTaskId())
                .build();
    }
}
