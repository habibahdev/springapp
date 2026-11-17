package com.habibah.springapp.dto.address;

/**
 * DTO représentant une adresse de livraison retournée par l'API
 * @param id identifiant de l'adresse
 * @param label libellé de l'adresse
 * @param recipientName nom du destinataire
 * @param street adresse postale
 * @param additionalAddress complément d'adresse
 * @param postalCode code postal
 * @param city ville
 * @param country pays
 * @param phone numéro de téléphone
 * @param defaultAddress indique si l'adresse est l'adresse par défaut
 */
public record AddressResponse(
        Long id,
        String label,
        String recipientName,
        String street,
        String additionalAddress,
        String postalCode,
        String city,
        String country,
        String phone,
        boolean defaultAddress
) {
}
