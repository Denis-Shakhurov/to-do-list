package org.example.fileservice.handler;

import org.example.fileservice.exception.FileStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException(FileStorageException ex) {
        String error = getError("FILE_STORAGE_ERROR", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<String> handleFileNotFoundException(FileNotFoundException ex) {
        String error = getError("FILE_NOT_FOUND", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    private String getError(String message, Exception ex) {
        return message + ", " + ex.getMessage() +
                ", " + Instant.now();
    }
}
