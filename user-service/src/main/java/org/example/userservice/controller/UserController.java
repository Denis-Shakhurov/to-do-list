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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        try {
            UserDTO userDTO = userService.getUser(id);
            userDTO.add(linkTo(methodOn(UserController.class)
                            .getUser(id))
                            .withSelfRel(),
                    linkTo(methodOn(UserController.class)
                            .createUser(null))
                            .withRel("createUser"),
                    linkTo(methodOn(UserController.class)
                            .updateUser(null, id))
                            .withRel("updateUser"),
                    linkTo(methodOn(UserController.class)
                            .deleteUser(id))
                            .withRel("deleteUser"));
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        UserDTO userDTO = userService.createUser(createDTO);
        userDTO.add(linkTo(methodOn(UserController.class)
                        .getUser(userDTO.getId()))
                        .withSelfRel(),
                linkTo(methodOn(UserController.class)
                        .updateUser(null, userDTO.getId()))
                        .withRel("updateUser"),
                linkTo(methodOn(UserController.class)
                        .deleteUser(userDTO.getId()))
                        .withRel("deleteUser"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userDTO);
    }

    @PostMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO,
                                              @PathVariable Long id) {
        try {
            UserDTO userDTO = userService.updateUser(updateDTO, id);
            userDTO.add(linkTo(methodOn(UserController.class)
                            .getUser(id))
                            .withSelfRel(),
                    linkTo(methodOn(UserController.class)
                            .createUser(null))
                            .withRel("createUser"),
                    linkTo(methodOn(UserController.class)
                            .deleteUser(id))
                            .withRel("deleteUser"));
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("User deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
