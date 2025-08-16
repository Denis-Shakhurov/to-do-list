package org.example.userservice.dto;

import lombok.Value;
import org.example.userservice.model.RoleUser;

/**
 * DTO for {@link org.example.userservice.model.User}
 */
@Value
public class UserUpdateDTO {
    Long id;
    String name;
    String email;
    String password;
    RoleUser role;
}