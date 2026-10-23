package com.habibah.springapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Représente les données envoyées par le client pour créer une commande.
 * <p>La requête contient les coordonnées du client, son adresse de livraison
 * et les articles qu'il souhaite commander. Chaque article est également
 * soumis aux validations définies dans {@link DetailRequest}</p>
 * @param customerName nom du client, obligatoire et non vide
 * @param customerEmail adresse e-mail du client, obligatoire et vide
 * @param shippingAddress adresse de livraison, obligatoire et non vide
 * @param items lites non vide des articles à commander, dont les éléments
 *              sont validé individuellement
 */
public record PurchaseRequest(
        @NotBlank String customerName,
        @NotBlank @Email String customerEmail,
        @NotBlank String shippingAddress,
        @NotEmpty @Valid List<DetailRequest> items
        ) {
}
