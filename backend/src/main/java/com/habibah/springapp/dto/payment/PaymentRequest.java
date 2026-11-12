package com.habibah.springapp.dto.payment;

import com.habibah.springapp.dto.detail.DetailRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Données nécessaires à la création d'un paiement.
 * @param shippingAddress adresse de livraison
 * @param items articles à commander
 */
public record PaymentRequest(
        @NotBlank String shippingAddress,
        @NotEmpty @Valid List<DetailRequest> items
        ) {
}
