package com.habibah.springapp.service;

import com.habibah.springapp.dto.profile.ProfileRequest;
import com.habibah.springapp.dto.profile.ProfileResponse;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.exception.BusinessException;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier chargé de la gestion du profil utilisateur.
 * <p>Il permet à un utilisateur authentifié de consulter et de modifier
 * ses infos personnelles, sans exposer ses données sensibles.</p>
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProfileService {
    private final UserRepository userRepository;

    /**
     * Modifie les infos personnelles de l'utilisateur
     * @param profileRequest nouvelles infos
     * @param currentEmail adresse e-mail actuelle de l'utilisateur
     * @return profil mis à jour
     */
    @Transactional
    public ProfileResponse updateProfile(ProfileRequest profileRequest, String currentEmail) {
        User user = findUser(currentEmail);
        String newEmail = profileRequest.email().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
            throw new BusinessException("Cet e-mail est déjà utilisé");
        }
        user.setFullName(profileRequest.fullName().trim());
        user.setEmail(newEmail);
        return toResponse(userRepository.save(user));
    }

    /**
     * Récupère le profil de l'utilisateur connecté
     * @param email adresse e-mail de l'utilisateur connecté
     * @return infos du profil
     */
    public ProfileResponse getProfile(String email) {
        User user = findUser(email);
        return toResponse(user);
    }

    /**
     * Recherche un utilisateur à partir de son adresse e-amil
     * @param email adresse e-mail recherchée
     * @return utilisateur trouvé
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    }

    /**
     * Convertit une entité utilisateur en réponse
     * @param user utilisateur à convertir
     * @return réponse contenant les infos
     */
    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail());
    }
}
