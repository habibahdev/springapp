package com.habibah.springapp.entity;

import com.habibah.springapp.entity.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Setter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Représente un utilisateur de la boutique en ligne.
 *
 * <p>Permet de stocker les infos nécessaires à l'identification d'un utilisateur
 * et à la gestion de ses droits d'accès à l'application.</p>
 *
 * <p>Chaque utilisateur possède une adresse e-mail unique et un rôle
 * qui détermine son niveau d'autorisation.</p>
 *
 * <p>Cette classe est associée à la table {@code users} en base de données.</p>
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    /**
     * Identifiant unique de l'utilisateur.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Adresse e-mail de l'utilisateur.
     * Elle doit être unique.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Nom complet de l'utilisateur.
     */
    @Column(nullable = false, name = "full_name")
    private String fullName;

    /**
     * Mot de passe de l'utilisateur.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Rôle attribué à l'utilisateur.
     * Les valeurs possibles sont définies dans l'numération {@link Role}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
