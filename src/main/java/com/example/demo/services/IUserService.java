package com.example.demo.services;

import com.example.demo.dto.UserRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserService {

    Mono<UserResponseDTO> createUser(UserRequestDTO request);

    Mono<UserResponseDTO> getUserById(String id);

    Flux<UserResponseDTO> getAllUsers(int page, int size);
}
