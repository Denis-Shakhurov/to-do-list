package org.example.taskservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.taskservice.controller.TaskController;
import org.example.taskservice.dto.TaskCreateDTO;
import org.example.taskservice.dto.TaskDTO;
import org.example.taskservice.dto.TaskUpdateDTO;
import org.example.taskservice.exception.ResourceNotFoundException;
import org.example.taskservice.model.TaskStatus;
import org.example.taskservice.security.JwtService;
import org.example.taskservice.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskControllerTest {
    private final String BASE_PATH = "/tasks/";
    private final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    @DisplayName("get valid task and returned status OK")
    public void getTaskTest() throws Exception {
        TaskDTO taskDTO = new TaskDTO(
                1L,
                "new",
                "first task",
                TaskStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(taskService.getTask(any(Long.class))).thenReturn(taskDTO);

        mockMvc.perform(get(BASE_PATH + "{id}", taskDTO.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(taskDTO.getTitle()));
    }

    @Test
    @DisplayName("get invalid task and returned status NOT FOUND")
    public void getInvalidTaskTest() throws Exception {
        when(taskService.getTask(any(Long.class)))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(get(BASE_PATH + "{id}", 999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("create task and returned status CREATED")
    public void createTaskTest() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO(
                "new task",
                "first task",
                TaskStatus.PENDING);

        TaskDTO expectedTask = new TaskDTO(
                1L,
                createDTO.getTitle(),
                createDTO.getDescription(),
                createDTO.getStatus(),
                LocalDateTime.now(),
                LocalDateTime.now());

        String jwt = "superToken";

        when(jwtService.resolveToken(any(HttpServletRequest.class))).thenReturn(jwt);
        when(jwtService.extractUserId(any(String.class))).thenReturn("1");
        when(taskService.createTask(any(Long.class), any(TaskCreateDTO.class)))
                .thenReturn(expectedTask);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(expectedTask.getDescription()));
    }

    @Test
    @DisplayName("create invalid data task and returned status BAD REQUEST")
    public void createInvalidTaskTest() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO(
                "",
                null,
                TaskStatus.PENDING);

        String jwt = "superToken";

        when(jwtService.resolveToken(any(HttpServletRequest.class))).thenReturn(jwt);
        when(jwtService.extractUserId(any(String.class))).thenReturn("1");
        when(taskService.createTask(any(Long.class), any(TaskCreateDTO.class)))
                .thenThrow(new RuntimeException("Bad request"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update task and returned status OK")
    public void updateTaskTest() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO(
                "title",
                "updated task"
                ,TaskStatus.COMPLETED);

        TaskDTO expectedTask = new TaskDTO(
                1L,
                updateDTO.getTitle(),
                updateDTO.getDescription(),
                updateDTO.getStatus(),
                LocalDateTime.now(),
                LocalDateTime.now());

        when(taskService.updateTask(any(TaskUpdateDTO.class), any(Long.class)))
                .thenReturn(expectedTask);

        mockMvc.perform(post(BASE_PATH + "{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(expectedTask.getTitle()));
    }

    @Test
    @DisplayName("update invalid data task and returned status BAD REQUEST")
    public void updateInvalidTaskTest() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO(
                "update",
                "new description",
                TaskStatus.PENDING);

        when(taskService.updateTask(any(TaskUpdateDTO.class), any(Long.class)))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(post(BASE_PATH + "{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete task and returned status NO CONTENT")
    public void deleteTaskTest() throws Exception {
        doNothing().when(taskService).deleteTask(any(Long.class));

        mockMvc.perform(delete(BASE_PATH + "{id}", 1L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete invalid task and returned status NOT FOUND")
    public void deleteInvalidTaskTest() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found"))
                .when(taskService).deleteTask(any(Long.class));

        mockMvc.perform(delete(BASE_PATH + "{id}", 999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
