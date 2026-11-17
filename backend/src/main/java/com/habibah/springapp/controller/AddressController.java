package com.habibah.springapp.controller;

import com.habibah.springapp.dto.address.AddressRequest;
import com.habibah.springapp.dto.address.AddressResponse;
import com.habibah.springapp.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

/**
 * Contrôleur REST chargé de la gestion des adresses de livraison.
 * <p>Toutes les opérations sont réalisées pour l'utilisateur authentifié.
 * SOn adresse e-mail est récupérée depuis le contexte de sécurité.</p>
 */
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@RestController
public class AddressController {
    private final AddressService addressService;

    /**
     * Récupère les adresses de l'utilisateur connecté.
     * @param authentication infos d'authentification
     * @return liste des adresses
     */
    @GetMapping
    public List<AddressResponse> addresses(Authentication authentication) {
        return addressService.getUserAddresses(authentication.getName());
    }

    /**
     * Crée une nouvelle adresse de livraison.
     * @param request infos de l'adresse
     * @param authentication infos d'authentification
     * @return adresse créée
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AddressResponse create(@Valid @RequestBody AddressRequest request, Authentication authentication) {
        return addressService.createAddress(request, authentication.getName());
    }

    /**
     * Modifie une adresse de livraison.
     * @param id identifiant de l'adresse
     * @param request nouvelles infos
     * @param authentication infos d'authentification
     * @return adresse modifiée
     */
    @PutMapping("/{id}")
    public AddressResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication
    ) {
        return addressService.updateAddress(id, request, authentication.getName());
    }

    /**
     * Supprime une adresse de livraison.
     * @param id identifiant de l'adresse
     * @param authentication infos d'authentification
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        addressService.deleteAddress(id, authentication.getName());
    }

    /**
     * Définit une adresse comme addresse de livraison par défaut.
     * @param id identifiant de l'adresse
     * @param authentication infos d'authentification
     * @return adresse définie par défaut
     */
    @PatchMapping("/{id}/default")
    public AddressResponse setDefaultAddress(@PathVariable Long id, Authentication authentication) {
        return addressService.setDefaultAddress(id, authentication.getName());
    }
}
