package com.habibah.springapp.dto;

import java.math.BigDecimal;

/**
 * Représente les infos d'un produit retournées par l'API.
 * <p>La réponse contient les caractéristiques du produit ainsi que
 * les infos de sa catégorie lorsqu'une catégorie est associée</p>
 * @param id identifiant unique du produit
 * @param name nom du produit
 * @param description description du produit
 * @param price prix unitaire du produit
 * @param stock quantité actuellement disponible en stock
 * @param imageUrl URL de l'image du produit
 * @param categoryId identifiant de la catégorie associée, ou {@code null}
 *                   si aucune catégorie n'est associée
 * @param categoryName nom de la catégorie associée, ou {@code null}
 *                     si aucune catégorie n'est associée
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String imageUrl,
        Long categoryId,
        String categoryName
) {
}
