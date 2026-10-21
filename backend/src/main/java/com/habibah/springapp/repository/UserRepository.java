package com.habibah.springapp.repository;

import com.habibah.springapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository permettant de gérer la persistance des utilisateurs.
 * <p>Cette interface fournit les opérations CRUD sur les entités {@link User}
 * et les méthodes de recherche spécifiques basées sur l'adresse e-mail.</p>
 * <p>Les requêtes associées aux méthodes personnalisées sont générées
 * automatiquement par Spring Data JPA</p>
 *
 * @see User
 * @see JpaRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Recherche un utilisateur à partir de son adresse e-mail.
     * <p>L'utilisation de {@link Optional} permet de représenter explicitement
     * le cas où aucun utilisateur ne correspond à l'adresse fournie.</p>
     *
     * @param email adresse e-mail de l'utilisateur recherché
     * @return un {@link Optional} contenant l'utilisateur trouvé,
     * ou un {@link Optional#empty()} si aucun utilisateur ne correspond
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un utilisateur possède déjà l'adresse e-mail fournie.
     * @param email adresse e-mail dont l'existence doit être vérifiée
     * @return {@code true} si un utilisateur possède cette adresse,
     * {@code false} sinon
     */
    boolean existsByEmail(String email);
}
