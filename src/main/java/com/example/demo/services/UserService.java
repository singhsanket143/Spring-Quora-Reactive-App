package com.example.demo.services;

import com.example.demo.adapter.UserAdapter;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserResponseDTO> createUser(UserRequestDTO request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .bio(request.getBio())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user)
                .map(UserAdapter::toUserResponseDTO);
    }

    @Override
    public Mono<UserResponseDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(UserAdapter::toUserResponseDTO);
    }

    @Override
    public Flux<UserResponseDTO> getAllUsers(int page, int size) {
        return userRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .map(UserAdapter::toUserResponseDTO);
    }
}
