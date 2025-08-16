package org.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.example.userservice.model.RoleUser;

/**
 * DTO for {@link org.example.userservice.model.User}
 */
@Value
public class UserCreateDTO {
    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @NotBlank
    @Email(message = "Некорректный email")
    @NotBlank(message = "email не может быть пустым")
    String email;
    @NotNull
    @NotBlank(message = "Пароль не должен быть пустым")
    String password;
    @NotNull
    RoleUser role;
}