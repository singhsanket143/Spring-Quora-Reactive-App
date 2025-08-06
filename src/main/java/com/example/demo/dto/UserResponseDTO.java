package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private String id;
    private String username;
    private String email;
    private String bio;
    private LocalDateTime createdAt;
}
