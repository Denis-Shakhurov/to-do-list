package org.example.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.example.taskservice.model.TaskStatus;

/**
 * DTO for {@link org.example.taskservice.model.Task}
 */
@Value
public class TaskCreateDTO {
    @NotNull
    @NotBlank
    String title;
    String description;
    TaskStatus status;
}