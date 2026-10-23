package com.habibah.springapp.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Représente les données nécessaires à la création ou à la modification d'un produit.
 * <p>Les contraintes de validation permettent notamment de vérifier la
 * présence du nom, la positivité du prix, la validité de la quantité
 * en stock et la longueur maximale de la description</p>
 * @param name nom du produit, obligatoire et non vide
 * @param description description du produit, limitée à 2000 caractères
 * @param price prix du produit, obligatoire et strictement positif
 * @param stock quantité en stock, supérieure ou égale à zéro
 * @param imageUrl URL de l'image du produit
 * @param categoryId identifiant de la catégorie associée, facultatif
 */
public record ProductRequest(
        @NotBlank String name,
        @Size(max = 2000) String description,
        @NotNull @Positive BigDecimal price,
        @Min(0) int stock,
        String imageUrl,
        Long categoryId
) {
}
