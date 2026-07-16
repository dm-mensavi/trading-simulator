package com.trading.userservice.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String username,
        String fullName,
        LocalDateTime createdAt
) {}
