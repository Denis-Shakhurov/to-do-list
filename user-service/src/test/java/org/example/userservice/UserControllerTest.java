package org.example.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.controller.UserController;
import org.example.userservice.dto.UserCreateDTO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.dto.UserUpdateDTO;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.model.RoleUser;
import org.example.userservice.service.UserService;
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
public class UserControllerTest {
    private final String BASE_PATH = "/users/";
    private final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("get valid user and returned userDTO")
    public void getUserTest() throws Exception {
        UserDTO userDTO = new UserDTO(1L,
                "Bob",
                "bob@mail.ru",
                RoleUser.USER,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(userService.getUser(any(Long.class))).thenReturn(userDTO);

        mockMvc.perform(get(BASE_PATH + "{id}", userDTO.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userDTO.getName()));
    }

    @Test
    @DisplayName("get invalid user and returned status NOT FOUND")
    public void getInvalidUserTest() throws Exception {
        UserDTO userDTO = new UserDTO(999L,
                "Alisa",
                "alisa@mail.ru",
                RoleUser.USER,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(userService.getUser(any(Long.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get(BASE_PATH + "{id}", userDTO.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("create valid user and returned userDTO")
    public void createUserTest() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO(
                1L,
                "Mark",
                "mark@mail.ru",
                RoleUser.USER);

        UserDTO expectedUser = new UserDTO(1L,
                "Mark",
                "mark@mail.ru",
                RoleUser.USER,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(expectedUser);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createDTO.getName()));
    }

    @Test
    @DisplayName("create invalid user and returned exception status")
    public void createInvalidUserTest() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO(
                1L,
                "",
                "mail.ru",
                RoleUser.USER);

        when(userService.createUser(any(UserCreateDTO.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update user and returned status Ok")
    public void updateUserTest() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                1L,
                "Bob",
                "bob@mail.ru",
                RoleUser.USER);

        UserDTO userDTO = new UserDTO(
                1L,
                "Bob",
                "bob@mail.ru",
                RoleUser.USER,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(userService.updateUser(any(UserUpdateDTO.class), any(Long.class)))
                .thenReturn(userDTO);

        mockMvc.perform(post(BASE_PATH + "{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userDTO.getName()));
    }

    @Test
    @DisplayName("update invalid user and returned status NOT FOUND")
    public void updateInvalidUserTest() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                1L,
                "",
                "mail.ru",
                RoleUser.USER);

        when(userService.updateUser(any(UserUpdateDTO.class), any(Long.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post(BASE_PATH + "{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete user and returned status NO CONTENT")
    public void deleteUserTest() throws Exception {
        doNothing().when(userService).deleteUser(any(Long.class));

        mockMvc.perform(delete(BASE_PATH + "{id}", 3L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete invalid user and returneds tatus NO FOUND")
    public void deleteInvalidUserTest() throws Exception {
        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteUser(any(Long.class));

        mockMvc.perform(delete(BASE_PATH + "{id}", 999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
