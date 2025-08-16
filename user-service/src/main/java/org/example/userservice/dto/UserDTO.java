package org.example.userservice.dto;

import lombok.Value;
import org.example.userservice.model.RoleUser;
import org.example.userservice.model.User;

import java.time.LocalDateTime;

/**
 * DTO for {@link User}
 */
@Value
public class UserDTO {
    Long id;
    String name;
    String email;
    RoleUser role;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}