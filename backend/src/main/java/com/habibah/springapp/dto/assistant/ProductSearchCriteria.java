package com.habibah.springapp.dto.assistant;

import java.math.BigDecimal;
import java.util.List;

/**
 * Représente les critères utilisés pour rechercher des produits à partir
 * d'une demande formulée en langage naturel
 * @param keywords mots-clés à rechercher dans le nom et la description
 * @param minPrice prix minimal souhaité, ou {@code null} si non précisé
 * @param maxPrice prix maximal souhaité, ou {@code null} si non précisé
 * @param categoryId identifiant de la catégorie souhaité, ou {@code null}
 * @param onlyAvailable indique si seuls les produits en stock doivent être retournés
 */
public record ProductSearchCriteria(
        List<String> keywords,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long categoryId,
        boolean onlyAvailable
) {

    /**
     * Normalise la liste des mots-clés pour éviter les valeurs nulles et les termes vides
     */
    public ProductSearchCriteria {
        keywords = keywords == null
                ? List.of()
                : keywords.stream().filter(keyword -> keyword != null && !keyword.isBlank()).map(String::trim).toList();
    }
}
