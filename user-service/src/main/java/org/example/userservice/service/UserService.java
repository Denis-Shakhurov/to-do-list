package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserCreateDTO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.dto.UserUpdateDTO;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found."));
        return userMapper.toUserDTO(user);
    }

    public UserDTO createUser(UserCreateDTO createDTO) {
        User user = userMapper.toEntity(createDTO);
        return userMapper.toUserDTO(userRepository.save(user));
    }

    public UserDTO updateUser(UserUpdateDTO updateDTO, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found."));
        userMapper.update(updateDTO, user);
        return userMapper.toUserDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found."));
        userRepository.delete(user);
    }
}
