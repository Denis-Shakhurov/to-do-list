package org.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.example.userservice.model.RoleUser;
import org.example.userservice.model.UserProfile;

/**
 * DTO for {@link UserProfile}
 */
@Value
public class UserCreateDTO {
    Long id;

    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @NotBlank
    @Email(message = "Некорректный email")
    @NotBlank(message = "email не может быть пустым")
    String email;
    @NotNull
    RoleUser role;
}