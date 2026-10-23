package com.habibah.springapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Représente les identifiants fournis par un utilisateur pour se connecter.
 * <p>L'adresse e-mail doi respecter un format valide et le mot de passe
 * doit être renseigné. La vérification effective des identifiants est
 * effectuée par le mécanisme d'authentification de Spring security</p>
 * @param email adresse e-mail utilisée comme identifiant de connexion
 * @param password mot de passe fourni lors de la connexion
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
