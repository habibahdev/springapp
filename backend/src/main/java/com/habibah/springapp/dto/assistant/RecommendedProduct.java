package com.habibah.springapp.dto.assistant;

import java.math.BigDecimal;

/**
 * Représente un produit proposé par l'assistant à un client
 * @param id identifiant du produit
 * @param name nom du produit
 * @param description description du produit
 * @param price prix du produit
 * @param available indique si le produit est disponible en stock
 * @param imageUrl url de l'image du produit
 * @param categoryName nom de la catégorie du produit
 */
public record RecommendedProduct(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        String imageUrl,
        String categoryName
) {
}
