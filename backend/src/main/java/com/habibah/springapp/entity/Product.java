package com.habibah.springapp.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Setter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Représente un produit commercialisé dans la boutique en ligne.
 *
 * <p>Cette entité regroupe les informations nécessaires à la présentation et
 * à la gestion commerciale d'un produit : nom, description, prix, stock disponible.</p>
 *
 * <p>Un produit peut appartenir à une catégorie. Cette association permet
 * d'organiser les articles dans le catalogue.</p>
 *
 * <p>Le prix est représenté par {@link BigDecimal} afin de manipuler les valeurs
 * monétaires avec une précision adaptée.</p>
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Product {
    /**
     * Identifiant unique du produit.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom commercial du produit.
     */
    @NotBlank
    private String name;

    /**
     * Description détaillée du produit.
     * La longueur maximale de la colonne est de 2000 caractères.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Prix unitaire du produit.
     */
    @NotNull
    @Positive
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Quantité de produits disponibles en stock.
     */
    @Min(0)
    private int stock;

    /**
     * URL ou chemin permettant d'accéder à l'image produit.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Catégorie à laquelle appartient le produit.
     * Cette association est facultative dans le modèle actuel.
     * Le chargement de la catégorie est effectué à la demande.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
