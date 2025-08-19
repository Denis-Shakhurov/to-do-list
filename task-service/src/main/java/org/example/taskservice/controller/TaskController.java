package org.example.taskservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.dto.TaskCreateDTO;
import org.example.taskservice.dto.TaskDTO;
import org.example.taskservice.dto.TaskUpdateDTO;
import org.example.taskservice.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable("taskId") Long taskId) {
        TaskDTO taskDTO = taskService.getTask(taskId);
        return ResponseEntity.ok(taskDTO);
    }

    @PostMapping("/")
    public ResponseEntity<TaskDTO> createTask(@RequestParam("userId") Long userId,
                                              @RequestBody TaskCreateDTO createDTO) {
        TaskDTO taskDTO = taskService.createTask(userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskDTO);
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable("taskId") Long taskId,
                                              @RequestBody TaskUpdateDTO updateDTO) {
        TaskDTO taskDTO = taskService.updateTask(updateDTO, taskId);
        return ResponseEntity.ok(taskDTO);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable("taskId") Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("Task deleted");
    }
}
