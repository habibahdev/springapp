package com.habibah.springapp.service;

import com.habibah.springapp.dto.AuthResponse;
import com.habibah.springapp.dto.LoginRequest;
import com.habibah.springapp.dto.RegisterRequest;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.enums.Role;
import com.habibah.springapp.exception.BusinessException;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.UserRepository;
import com.habibah.springapp.service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier chargé de l'inscription et de la connexion des utilisateurs.
 * <p>Il vérifie la disponibilité de l'adresse e-mail lors de l'inscription,
 * encode le mot de passe avant son stockage et délègue l'authentification
 * à Spring Security lors de la connexion.</p>
 * <p>Après une inscription ou une connexion réussie, un jeton JWT est généré et
 * retourné au client avec les informations publiques de l'utilisateur.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     * Gestionnaire chargé de vérifier les identifiants lors de la connexion.
     */
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Inscrit un nouvel utilisateur.
     * <p>L'adresse e-mail est normalisée, le mot de passe encodé
     * et le rôle {@link Role#USER} est attribué par défaut</p>
     * @param request données nécessaires à la création du compte
     * @return réponse contenant le JWT et les infos de l'utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(Role.USER)
                .build());
        return toResponse(user);
    }

    /**
     * Authentifie un utilisateur à partir de son adresse e-mail et de son mot de passe.
     * <p>Les identifiants sont normalisés puis vérifiés par Spring Security. SI
     * l'authentification réussit, l'utilisateur est recherché afin de construire
     * la résponse contenant sont JWT.</p>
     * @param request identifiants de connexion
     * @return réponse contenant le JWT et les infos de l'utilisateur
     */
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        return toResponse(user);
    }

    /**
     * Construit la réponse d'authentification à partir d'un utilisateur.
     * <p>Un nouveau JWT est généré avec l'adresse e-mail et le rôle
     * de l'utilisateur.</p>
     * @param user utilisateur pour lequel la réponse doit être créée
     * @return réponse contenant le jeton et les infos publiques de l'utilisateur
     */
    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole());
    }
}
