package com.habibah.springapp.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données nécessaires à la modification du profil utilisateur.
 * @param fullName nouveau nom complet
 */
public record ProfileRequest(
        @NotBlank @Size(max = 255)
        String fullName
) {
}
