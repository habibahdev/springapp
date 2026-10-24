package com.habibah.springapp.controller;

import com.habibah.springapp.dto.AuthResponse;
import com.habibah.springapp.dto.LoginRequest;
import com.habibah.springapp.dto.RegisterRequest;
import com.habibah.springapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Contrôleur REST responsable de l'authentification des utilisateurs.
 * <p>Expose les endpoints permettant de créer un compte utilisateur
 * et de se connecter à l'application.</p>
 * <p>Les traitements d'inscription et d'authentification sont délégués
 * à {@link AuthService}</p>
 */
@Tag(
        name = "Authentification",
        description = "Inscription et connexion des utilisateurs"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    /**
     * Service chargé de l'inscription et de l'authentification des utilisateurs
     */
    private final AuthService authService;

    /**
     * Inscrit un nouvel utilisateur.
     * <p>Les données de la requête sont validées avant leur transmission
     * au service d'authentification.</p>
     * @param request informations nécessaires à la création du compte
     * @return les informations d'authentification retournées après l'inscription,
     * notamment le jeton d'accès si le service en génère un
     */
    @Operation(
            summary = "Inscrire un utilisateur",
            description = "Crée un nouveau compte utilisateur et retourne "
            + "les infos d'authentification"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Compte créé avec succès"
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Données invalides",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Adresse e-mail déjà utilisée",
                    content = @Content
            )
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Authentifie un utilisateur à partir de ses identifiants.
     * <p>Les informations de connexion sont validées avant d'être transmises
     * au service. La réponse contient les infos d'authentification produites
     * par ce dernier.</p>
     * @param request identifiants de connexion de l'utilisateur
     * @return les infos d'authentification, notamment le jeton d'accès
     * si l'authentification réussit
     */
    @Operation(
            summary = "Connecter un utilisateur",
            description = "Authentifie un utilisateur et retourne "
            + "les infos d'authentification"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentification réussie"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de connexion invalides",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Identifiants incorrects",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
