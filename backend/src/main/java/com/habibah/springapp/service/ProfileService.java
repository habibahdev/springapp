package com.habibah.springapp.service;

import com.habibah.springapp.dto.profile.PasswordChangeRequest;
import com.habibah.springapp.dto.profile.ProfileRequest;
import com.habibah.springapp.dto.profile.ProfileResponse;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.exception.BusinessException;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier chargé de la gestion du profil utilisateur.
 * <p>
 * Il permet à un utilisateur authentifié de consulter et de modifier
 * son nom complet. L'adresse e-mail est conservée et ne peut pas être
 * modifiée depuis cette fonctionnalité.
 * </p>
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Modifie le mot de passe de l'utilisateur connecté.
     *
     * @param request informations nécessaires au changement du mot de passe
     * @param email adresse e-mail de l'utilisateur connecté
     * @throws NotFoundException si l'utilisateur est introuvable
     * @throws BusinessException si le mot de passe actuel est incorrect,
     * si les nouveaux mots de passe ne correspondent pas
     * ou si le nouveau mot de passe est identique à l'ancien
     */
    @Transactional
    public void changePassword(
            final PasswordChangeRequest request,
            final String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Le mot de passe actuel est incorrect.");
        }
        if (!request.newPassword().equals(request.confirmation())) {
            throw new BusinessException("Les nouveaux mots de passe ne correspondent pas.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(
                    "Le nouveau mot de passe doit être différent de l'ancien."
            );
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /**
     * Modifie le nom complet de l'utilisateur connecté.
     *
     * @param profileRequest nouvelles informations du profil
     * @param email adresse e-mail de l'utilisateur connecté
     * @return profil mis à jour
     * @throws NotFoundException si l'utilisateur est introuvable
     */
    @Transactional
    public ProfileResponse updateProfile(
            final ProfileRequest profileRequest,
            final String email
    ) {
        User user = findUser(email);
        user.setFullName(profileRequest.fullName());
        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    /**
     * Récupère le profil de l'utilisateur connecté.
     *
     * @param email adresse e-mail de l'utilisateur connecté
     * @return informations du profil
     * @throws NotFoundException si l'utilisateur est introuvable
     */
    public ProfileResponse getProfile(final String email) {
        User user = findUser(email);
        return toResponse(user);
    }

    /**
     * Recherche un utilisateur à partir de son adresse e-mail.
     *
     * @param email adresse e-mail recherchée
     * @return utilisateur trouvé
     * @throws NotFoundException si aucun utilisateur ne correspond à cette adresse
     */
    private User findUser(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    }

    /**
     * Convertit une entité utilisateur en réponse DTO.
     *
     * @param user utilisateur à convertir
     * @return réponse contenant les informations du profil
     */
    private ProfileResponse toResponse(final User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}