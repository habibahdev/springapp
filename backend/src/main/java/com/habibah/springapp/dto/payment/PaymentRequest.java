package com.habibah.springapp.dto.payment;

import com.habibah.springapp.dto.detail.DetailRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO contenant les infos nécessaires à la création d'une commande
 * et à l'initialisation de son paiement.
 * @param shippingAddress adresse de livraison obligatoire et non vide
 * @param items articles à commander
 */
public record PaymentRequest(
        @NotBlank String shippingAddress,
        @NotEmpty @Valid List<DetailRequest> items
        ) {
}
