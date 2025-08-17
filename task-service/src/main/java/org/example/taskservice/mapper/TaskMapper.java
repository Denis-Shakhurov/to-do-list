package org.example.taskservice.mapper;

import org.example.taskservice.dto.TaskCreateDTO;
import org.example.taskservice.dto.TaskDTO;
import org.example.taskservice.dto.TaskUpdateDTO;
import org.example.taskservice.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {
    Task toEntity(TaskDTO taskDTO);

    TaskDTO toTaskDTO(Task task);

    Task toEntity(TaskCreateDTO taskCreateDTO);

    TaskCreateDTO toTaskCreateDTO(Task task);

    void update(TaskUpdateDTO updateDTO,@MappingTarget Task task);
}