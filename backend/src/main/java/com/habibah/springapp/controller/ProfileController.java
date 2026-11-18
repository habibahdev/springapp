package com.habibah.springapp.controller;

import com.habibah.springapp.dto.profile.PasswordChangeRequest;
import com.habibah.springapp.dto.profile.ProfileRequest;
import com.habibah.springapp.dto.profile.ProfileResponse;
import com.habibah.springapp.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST permettant à un utilisateur authentifié
 * de consulter et de modifier son profil.
 * <p>
 * L'adresse e-mail est consultable, mais ne peut pas être modifiée
 * depuis ces endpoints.
 * </p>
 */
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@RestController
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Récupère le profil de l'utilisateur connecté.
     *
     * @param authentication informations d'authentification
     * @return profil de l'utilisateur connecté
     */
    @GetMapping
    public ProfileResponse getProfile(final Authentication authentication) {
        return profileService.getProfile(authentication.getName());
    }

    /**
     * Modifie le nom complet de l'utilisateur connecté.
     *
     * @param request nouvelles informations du profil
     * @param authentication informations d'authentification
     * @return profil mis à jour
     */
    @PutMapping
    public ProfileResponse update(
            @Valid @RequestBody final ProfileRequest request,
            final Authentication authentication
    ) {
        return profileService.updateProfile(request, authentication.getName());
    }

    /**
     * Modifie le mot de passe de l'utilisateur connecté.
     *
     * @param request informations nécessaires au changement du mot de passe
     * @param authentication informations d'authentification
     */
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody final PasswordChangeRequest request,
            final Authentication authentication
    ) {
        profileService.changePassword(request, authentication.getName());
    }
}