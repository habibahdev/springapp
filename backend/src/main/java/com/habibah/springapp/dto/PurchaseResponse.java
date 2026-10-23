package com.habibah.springapp.dto;

import com.habibah.springapp.entity.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Représente les infos d'une commande retournées par l'API.
 * @param id identifiant unique de la commande
 * @param customerName nom du client associé à la commande
 * @param customerEmail adresse e-mail du client
 * @param shippingAddress adresse de livraison de la commande
 * @param status statut actuel de la commande
 * @param createdAt date et heure de création de la commande
 * @param total montant total de la commande
 * @param items liste des articles composant la commande
 */
public record PurchaseResponse(
        Long id,
        String customerName,
        String customerEmail,
        String shippingAddress,
        PurchaseStatus status,
        LocalDateTime createdAt,
        BigDecimal total,
        List<DetailResponse> items
) {
}
