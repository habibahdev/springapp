package com.habibah.springapp.dto.payment;

import com.habibah.springapp.dto.detail.DetailRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO contenant les infos nécessaires à la création d'une commande
 * et à l'initialisation de son paiement.
 * @param addressId identifiant de l'adresse de livraison
 * @param items articles à commander
 */
public record PaymentRequest(
        @NotNull Long addressId,
        @NotEmpty @Valid List<DetailRequest> items
        ) {
}
