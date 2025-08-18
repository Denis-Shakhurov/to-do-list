package org.example.fileservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UploadFileRequest {
    @NotNull
    private MultipartFile file;

    private Long taskId;
}
