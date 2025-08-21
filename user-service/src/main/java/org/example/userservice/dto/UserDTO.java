package org.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import org.example.userservice.model.RoleUser;
import org.example.userservice.model.UserProfile;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

/**
 * DTO for {@link UserProfile}
 */
@Value
public class UserDTO extends RepresentationModel<UserDTO> {
    Long id;
    String name;
    String email;
    RoleUser role;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime updatedAt;
}