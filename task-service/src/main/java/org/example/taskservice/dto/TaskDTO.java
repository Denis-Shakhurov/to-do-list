package org.example.taskservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import org.example.taskservice.model.TaskStatus;

import java.time.LocalDateTime;

/**
 * DTO for {@link org.example.taskservice.model.Task}
 */
@Value
public class TaskDTO {
    Long id;
    String title;
    String description;
    TaskStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}