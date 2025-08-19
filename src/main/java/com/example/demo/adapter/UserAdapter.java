package com.example.demo.adapter;

import com.example.demo.dto.UserResponseDTO;
import com.example.demo.models.User;

public class UserAdapter {

    public static UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
