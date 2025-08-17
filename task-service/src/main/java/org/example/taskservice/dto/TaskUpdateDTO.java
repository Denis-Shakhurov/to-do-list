package org.example.taskservice.dto;

import lombok.Value;
import org.example.taskservice.model.TaskStatus;

/**
 * DTO for {@link org.example.taskservice.model.Task}
 */
@Value
public class TaskUpdateDTO {
    String title;
    String description;
    TaskStatus status;
}