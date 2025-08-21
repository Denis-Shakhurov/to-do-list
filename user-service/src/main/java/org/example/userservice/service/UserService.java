package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserCreateDTO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.dto.UserUpdateDTO;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.UserProfile;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO getUser(Long id) {
        UserProfile userProfile = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found."));
        return userMapper.toUserDTO(userProfile);
    }

    public UserDTO createUser(UserCreateDTO createDTO) {
        UserProfile userProfile = userMapper.toEntity(createDTO);
        return userMapper.toUserDTO(userRepository.save(userProfile));
    }

    public UserDTO updateUser(UserUpdateDTO updateDTO, Long id) {
        UserProfile userProfile = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found."));
        userMapper.update(updateDTO, userProfile);
        return userMapper.toUserDTO(userRepository.save(userProfile));
    }

    public void deleteUser(Long id) {
        UserProfile userProfile = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found."));
        userRepository.delete(userProfile);
    }
}
