package com.habibah.springapp.dto.profile;

/**
 * Infos publiques du profil utilisateur.
 * @param id identifiant de l'utilisateur
 * @param fullName nom complet
 * @param email adresse e-mail
 */
public record ProfileResponse(
        Long id,
        String fullName,
        String email
) {
}
