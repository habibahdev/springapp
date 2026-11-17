package com.habibah.springapp.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO contenant les infos nécessaires à la création ou à la modification
 * d'une adresse de livraison
 * @param label libellé de l'adresse
 * @param recipientName nom du destinataire
 * @param street adresse postale
 * @param additionalAddress complément d'adresse
 * @param postalCode code postal
 * @param city ville
 * @param country pays
 * @param phone numéro de téléphone
 * @param defaultAddress indique si l'adresse doit être définie par défaut
 */
public record AddressRequest(
        @NotBlank @Size(max = 50)
        String label,

        @NotBlank
        String recipientName,

        @NotBlank
        String street,

        String additionalAddress,

        @NotBlank @Size(max = 20)
        String postalCode,

        @NotBlank @Size(max = 100)
        String city,

        @NotBlank @Size(max = 100)
        String country,

        @NotBlank @Size(max = 30)
        String phone,

        boolean defaultAddress
) {
}
