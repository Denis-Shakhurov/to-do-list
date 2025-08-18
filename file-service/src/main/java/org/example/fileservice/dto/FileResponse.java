package org.example.fileservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileResponse {
    private Long id;
    private String originalName;
    private String contentType;
    private Long size;
    private LocalDateTime uploadDate;
    private Long taskId;
}
