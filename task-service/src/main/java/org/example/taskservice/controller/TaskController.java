package org.example.taskservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskservice.dto.TaskCreateDTO;
import org.example.taskservice.dto.TaskDTO;
import org.example.taskservice.dto.TaskUpdateDTO;
import org.example.taskservice.security.JwtService;
import org.example.taskservice.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final JwtService jwtService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable("taskId") Long taskId) {
        try {
            TaskDTO taskDTO = taskService.getTask(taskId);
            return ResponseEntity.ok(taskDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskCreateDTO createDTO,
                                              HttpServletRequest request) {
        try {
            String jwt = jwtService.resolveToken(request);
            String userId = jwtService.extractUserId(jwt);

            TaskDTO taskDTO = taskService.createTask(Long.parseLong(userId), createDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(taskDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable("taskId") Long taskId,
                                              @Valid @RequestBody TaskUpdateDTO updateDTO) {
        try {
            TaskDTO taskDTO = taskService.updateTask(updateDTO, taskId);
            return ResponseEntity.ok(taskDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable("taskId") Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Task deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
