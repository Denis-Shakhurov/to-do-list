package org.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserCreateDTO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.dto.UserUpdateDTO;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO userDTO = userService.getUser(id);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        UserDTO userDTO = userService.createUser(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userDTO);
    }

    @PostMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO,
                                              @PathVariable Long id) {
        UserDTO userDTO = userService.updateUser(updateDTO, id);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("User deleted");
    }
}
