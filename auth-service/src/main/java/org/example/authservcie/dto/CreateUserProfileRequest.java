package org.example.authservcie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.authservcie.model.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserProfileRequest {
    private Long id;
    private String name;
    private String email;
    private Role role;
}
