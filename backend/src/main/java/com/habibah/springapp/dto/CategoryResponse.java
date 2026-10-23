package com.habibah.springapp.dto;

/**
 * Représente les infos d'une catégorie de produits retournées par l'API.
 * @param id identifiant unique de la catégorie
 * @param name nom de la catégorie
 */
public record CategoryResponse(
        Long id,
        String name
) {
}
