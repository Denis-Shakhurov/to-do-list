package org.example.fileservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fileservice.controller.FileController;
import org.example.fileservice.dto.FileResponse;
import org.example.fileservice.exception.FileStorageException;
import org.example.fileservice.model.FileMetadata;
import org.example.fileservice.security.JwtService;
import org.example.fileservice.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.io.FileNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FileControllerTest {
    private final String BASE_PATH = "/files";
    private final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
    }

    @Test
    @DisplayName("uploadFile_ShouldReturnFileResponse_WhenValidFile")
    public void uploadFileTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        FileMetadata fileMetadata = FileMetadata.builder()
                .id(1L)
                .originalFileName(file.getName())
                .storageFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(11L)
                .taskId(456L)
                .build();

        when(jwtService.resolveToken(any(HttpServletRequest.class))).thenReturn("valid-token");
        when(jwtService.extractUserId("valid-token")).thenReturn("123");
        when(fileStorageService.uploadFile(any(), eq(123L), eq(456L)))
                .thenReturn(fileMetadata);

        mockMvc.perform(multipart(BASE_PATH)
                        .file(file)
                        .param("taskId", "456")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.originalName").value(file.getName()))
                .andExpect(jsonPath("$.contentType").value(file.getContentType()))
                .andExpect(jsonPath("$.size").value(file.getSize()))
                .andExpect(jsonPath("$.taskId").value(456L));

        verify(jwtService, times(1)).resolveToken(any());
        verify(jwtService, times(1)).extractUserId("valid-token");
        verify(fileStorageService, times(1)).uploadFile(any(), eq(123L), eq(456L));
    }

    @Test
    @DisplayName("getFileMetadata_ShouldReturnFileResponse_WhenFileExists")
    public void getFileTest() throws Exception {
        Long fileId = 1L;
        FileMetadata fileMetadata = FileMetadata.builder()
                .id(fileId)
                .originalFileName("document.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .taskId(789L)
                .build();

        when(fileStorageService.getFileMetadata(fileId)).thenReturn(fileMetadata);

        mockMvc.perform(get(BASE_PATH + "/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId))
                .andExpect(jsonPath("$.originalName").value("document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.size").value(1024L))
                .andExpect(jsonPath("$.taskId").value(789L));

        verify(fileStorageService, times(1)).getFileMetadata(fileId);
    }

    @Test
    @DisplayName("getFileMetadata_ShouldReturnNotFound_WhenFileNotExists")
    public void getInvalidFileTest() throws Exception {
        Long fileId = 999L;
        when(fileStorageService.getFileMetadata(fileId))
                .thenThrow(new FileStorageException("File not found"));

        mockMvc.perform(get(BASE_PATH + "/{fileId}", fileId))
                .andExpect(status().isNotFound());

        verify(fileStorageService, times(1)).getFileMetadata(fileId);
    }

    @Test
    @DisplayName("generateDownloadUrl_ShouldReturnDownloadUrl_WhenFileExists")
    public void generateDownloadUrlTest() throws Exception {
        Long fileId = 1L;
        String downloadUrl = "https://storage.example.com/files/1/download?token=abc123";

        when(fileStorageService.generatePresignedUrl(fileId)).thenReturn(downloadUrl);

        mockMvc.perform(get(BASE_PATH + "/{fileId}/download", fileId))
                .andExpect(status().isOk())
                .andExpect(content().string(downloadUrl));

        verify(fileStorageService, times(1)).generatePresignedUrl(fileId);
    }

    @Test
    @DisplayName("generateDownloadUrl_ShouldReturnInternalServerError_WhenServiceFails")
    public void invalidGenerateDownloadUrlTest() throws Exception {
        Long fileId = 1L;
        when(fileStorageService.generatePresignedUrl(fileId))
                .thenThrow(new FileStorageException("Failed to generate URL"));

        mockMvc.perform(get(BASE_PATH + "/{fileId}/download", fileId))
                .andExpect(status().isNotFound());

        verify(fileStorageService, times(1)).generatePresignedUrl(fileId);
    }

    @Test
    @DisplayName("deleteFile_ShouldReturnNoContent_WhenFileDeleted")
    void deleteFileTest() throws Exception {
        Long fileId = 1L;
        doNothing().when(fileStorageService).deleteFile(fileId);

        mockMvc.perform(delete("/files/{fileId}", fileId))
                .andExpect(status().isNoContent());

        verify(fileStorageService, times(1)).deleteFile(fileId);
    }

    @Test
    @DisplayName("deleteFile_ShouldReturnInternalServerError_WhenServiceFails")
    void invalidDeleteFileTest() throws Exception {

        Long fileId = 1L;
        doThrow(new FileStorageException("Failed to delete file"))
                .when(fileStorageService).deleteFile(fileId);

        mockMvc.perform(delete(BASE_PATH + "/{fileId}", fileId))
                .andExpect(status().isNotFound());

        verify(fileStorageService, times(1)).deleteFile(fileId);
    }

}
