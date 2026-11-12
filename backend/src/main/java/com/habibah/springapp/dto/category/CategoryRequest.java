package com.habibah.springapp.dto.category;

import jakarta.validation.constraints.NotBlank;

/**
 * Représente les données nécessaires à la création ou à la modification
 * d'une catégorie de produits
 * @param name nom de la catégorie, obligatoire et non vide
 */
public record CategoryRequest(
        @NotBlank String name
) {
}
