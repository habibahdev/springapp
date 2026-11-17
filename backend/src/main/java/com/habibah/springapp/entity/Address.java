package com.habibah.springapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Représente une adresse de livraison enregistrée par un utilisateur.
 * <p>Un utilisateur peut enregistrer plusieurs adresses afin de les
 * réutiliser lors de ses commandes.</p>
 * <p>Une adresse appartient obligatoirement à un utilisateur.
 * Elle peut également être définie comme une adresse de livraison par défaut.</p>
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "address")
public class Address {
    /**
     * Identifiant unique de l'adresse
     */
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

    /**
     * Adresse postale principale.
     */
    @Column(nullable = false)
    private String street;

    /**
     * Complément d'adresse facultatif.
     */
    @Column(name = "additional_address")
    private String additionalAddress;

    /**
     * Code postal de l'adresse.
     */
    @Column(nullable = false, name = "postal_code")
    private String postalCode;

    /**
     * Ville de l'adresse.
     */
    @Column(nullable = false)
    private String city;

    /**
     * Pays.
     */
    @Column(nullable = false)
    private String country;

    /**
     * Numéro de téléphone de l'utilisateur.
     */
    private String phone;

    /**
     * Indique si cette adresse est l'adresse de livraison par défaut.
     */
    @Column(nullable = false, name = "is_default")
    private boolean defaultAddress = false;

    /**
     * Propriétaire de l'adresse.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
