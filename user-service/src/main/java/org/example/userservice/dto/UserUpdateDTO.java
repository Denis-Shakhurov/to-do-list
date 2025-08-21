package org.example.userservice.dto;

import lombok.Value;
import org.example.userservice.model.RoleUser;
import org.example.userservice.model.UserProfile;

/**
 * DTO for {@link UserProfile}
 */
@Value
public class UserUpdateDTO {
    Long id;
    String name;
    String email;
    RoleUser role;
}