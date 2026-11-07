package com.habibah.springapp.dto.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Représente une demande envoyée à l'assistant
 * @param message message formulé par le client
 */
public record ChatRequest(
        @NotBlank(message = "le message ne peut pas être vide")
        @Size(max = 1000, message = "le message ne doit pas dépasser 1000 caractères")
        String message
) {
}
