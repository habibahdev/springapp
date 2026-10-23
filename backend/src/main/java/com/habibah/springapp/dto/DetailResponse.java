package com.habibah.springapp.dto;

import java.math.BigDecimal;

/**
 * Représente une ligne de commande dans une réponse de l'API.
 * <p>Chaque ligne identifie le produit commandé et précise la quantité
 * ainsi que le prix unitaire enregistré pour la commande.</p>
 * @param productId identifiant du produit commandé
 * @param productName nom du produit au moment de la représentation de la commande
 * @param quantity quantité de produits commandée
 * @param unitPrice prix unitaire appliqué à cette ligne de commande
 */
public record DetailResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
