package com.habibah.springapp.dto;

import com.habibah.springapp.entity.enums.Role;

/**
 * Représente la réponse envoyée au client après une authentification réussie.
 * <p>Elle contient le jeton jwt permettant d'effectuer des requêtes
 * authentifiées, ainsi que les infos publiques de l'utilisateur.</p>
 * @param token jeton jwt utilisé pour authentifier les requêtes suivantes
 * @param email adresse e-mail de l'utilisateur authentifié
 * @param fullName nom complet de l'utilisateur
 * @param role rôle attribué à l'utilisateur dans l'application
 */
public record AuthResponse(
        String token,
        String email,
        String fullName,
        Role role
) {
}
