package com.habibah.springapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Représente les données nécessaires à l'inscription d'un nouvel utilisateur.
 * @param fullName nom complet de l'utilisateur, obligatoire et non vide
 * @param email adresse e-mail de l'utilisateur, obligatoire et au format valide
 * @param password mot de passe, obligatoire et limité à 8 à 72 caractères
 */
public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
