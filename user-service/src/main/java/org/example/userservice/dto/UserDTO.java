package org.example.userservice.dto;

import lombok.Value;
import org.example.userservice.model.RoleUser;
import org.example.userservice.model.User;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

/**
 * DTO for {@link User}
 */
@Value
public class UserDTO extends RepresentationModel<UserDTO> {
    Long id;
    String name;
    String email;
    RoleUser role;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}