package com.habibah.springapp.dto.assistant;

import java.util.List;

/**
 * Représente la réponse retournée par l'assistant
 * @param message réponse textuelle
 * @param products liste de produits correspondant à la recherche
 */
public record ChatResponse(
        String message,
        List<RecommendedProduct> products
) {
}
