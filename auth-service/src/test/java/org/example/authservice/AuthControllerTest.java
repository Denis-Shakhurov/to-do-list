package org.example.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.authservcie.controller.AuthController;
import org.example.authservcie.dto.AuthRequest;
import org.example.authservcie.dto.AuthResponse;
import org.example.authservcie.dto.RegisterRequest;
import org.example.authservcie.exception.AlreadyExistsException;
import org.example.authservcie.handler.GlobalExceptionHandler;
import org.example.authservcie.model.Role;
import org.example.authservcie.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthControllerTest {
    private final String BASE_PATH = "/auth/";
    private final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    @DisplayName("register_ShouldReturnAuthResponse_WhenValidRequest")
    public void registerValidTest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        AuthResponse expectedResponse = AuthResponse.builder()
                .id(1L)
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post(BASE_PATH + "register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"));
    }

    @Test
    @DisplayName("register_ShouldReturnBadRequest_WhenInvalidRequest")
    public void registerInvalidTest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("")
                .email("email.com")
                .password("123")
                .role(Role.ADMIN)
                .build();

        mockMvc.perform(post(BASE_PATH + "register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("login_ShouldReturnAuthResponse_WhenValidCredentials")
    public void loginValidTest() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("test@mail.com")
                .password("currentPassword")
                .build();

        AuthResponse expectedResponse = AuthResponse.builder()
                .id(1L)
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        when(authService.login(any(AuthRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post(BASE_PATH + "login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    @DisplayName("login_ShouldReturnBadRequest_WhenInvalidRequest")
    public void loginInvalidTest() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("testmail.com")
                .password("curr")
                .build();

        mockMvc.perform(post(BASE_PATH + "login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(AuthRequest.class));
    }

    @Test
    @DisplayName("validateToken_ShouldReturnTrue_WhenValidToken")
    public void validateTokenTest() throws Exception {
        String token = "valid-token";
        when(authService.validateToken(token)).thenReturn(true);

        mockMvc.perform(get(BASE_PATH + "validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(authService, times(1)).validateToken(token);
    }

    @Test
    @DisplayName("validateToken_ShouldReturnFalse_WhenInvalidToken")
    public void invalidateTokenTest() throws Exception {
        String token = "invalid-token";
        when(authService.validateToken(token)).thenReturn(false);

        mockMvc.perform(get(BASE_PATH + "validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(authService, times(1)).validateToken(token);
    }

    @Test
    @DisplayName("register_ShouldHandleServiceException")
    public void registerHandleServiceExceptionTest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new AlreadyExistsException("User already exists"));

        mockMvc.perform(post(BASE_PATH + "register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("login_ShouldHandleInvalidCredentials")
    public void loginInvalidCredentialsTest() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("testmail.com")
                .password("curr")
                .build();

        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post(BASE_PATH + "login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
