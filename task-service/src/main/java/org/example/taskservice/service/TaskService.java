package org.example.taskservice.service;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.dto.TaskCreateDTO;
import org.example.taskservice.dto.TaskDTO;
import org.example.taskservice.dto.TaskUpdateDTO;
import org.example.taskservice.exception.ResourceNotFoundException;
import org.example.taskservice.mapper.TaskMapper;
import org.example.taskservice.model.Task;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public TaskDTO getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " not found"));
        return taskMapper.toTaskDTO(task);
    }

    public TaskDTO createTask(Long userId,TaskCreateDTO createDTO) {
        Task task = taskMapper.toEntity(createDTO);
        task.setId(userId);
        taskRepository.save(task);
        return taskMapper.toTaskDTO(task);
    }

    public TaskDTO updateTask(TaskUpdateDTO updateDTO, Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " not found"));
        taskMapper.update(updateDTO, task);
        taskRepository.save(task);
        return taskMapper.toTaskDTO(task);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " not found"));
        taskRepository.delete(task);
    }
}
