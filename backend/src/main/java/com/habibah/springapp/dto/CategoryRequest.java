package com.habibah.springapp.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name
) {
}
