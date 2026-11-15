package com.habibah.springapp.dto.payment;

/**
 * DTO représentant les informations retournées après la crèation
 * d'une session Stripe Checkout
 * @param purchaseId identifiant de la commande
 * @param sessionId identifiant de la session Stripe
 * @param checkoutUrl url de redirection vers Stripe
 */
public record CheckoutResponse(
        Long purchaseId,
        String sessionId,
        String checkoutUrl
) {
}
