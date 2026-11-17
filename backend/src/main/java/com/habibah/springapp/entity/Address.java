package com.habibah.springapp.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une adresse d'un utilisateur.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Libellé permettant d'identifier l'adresse (domicile, travail, ...)
     */
    @Column(nullable = false, length = 50)
    private String label;

    /**
     * Nom de la personne qui va réceptionner la commande.
     */
    @Column(nullable = false, name = "recipient_name")
    private String recipientName;

    @Column(nullable = false)
    private String street;

    @Column(name = "additional_address")
    private String additionalAddress;

    @Column(nullable = false, name = "postal_code")
    private String postalCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String phone;

    /**
     * Indique si cette adresse est l'adresse de livraison par défaut.
     */
    @Column(nullable = false, name = "is_default")
    private boolean defaultAddress = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
