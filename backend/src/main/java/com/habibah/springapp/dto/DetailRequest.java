package com.habibah.springapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Représente un article demandé dans une requête de création de commande.
 * <p>Le produit est identifié par son identifiant et la quantité demandée
 * doit être supérieure ou égale à un.</p>
 * @param productId identifiant du produit à commander, obligatoire
 * @param quantity quantité de produits souhaitée, au minimum égale à un
 */
public record DetailRequest(
        @NotNull Long productId,
        @Min(1) int quantity
) {
}
