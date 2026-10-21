package com.habibah.springapp.dto;

import com.habibah.springapp.entity.enums.Role;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        Role role
) {
}
