package com.habibah.springapp.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données nécessaires au changement du mot de passe
 * @param currentPassword mot de passe actuel
 * @param newPassword nouveau mot de passe
 * @param confirmation confirmation du nouveau mot de passe
 */
public record PasswordChangeRequest(
        @NotBlank
        String currentPassword,

        @NotBlank @Size(min = 8, max = 100)
        String newPassword,

        @NotBlank
        String confirmation
) {
}
