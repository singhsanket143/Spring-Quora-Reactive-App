package com.example.demo.controllers;

import com.example.demo.dto.UserRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.services.IUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    @PostMapping
    public Mono<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    public Mono<UserResponseDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public Flux<UserResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getAllUsers(page, size);
    }
}
