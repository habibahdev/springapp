package com.habibah.springapp.service;

import com.habibah.springapp.dto.address.AddressRequest;
import com.habibah.springapp.dto.address.AddressResponse;
import com.habibah.springapp.entity.Address;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.AddressRepository;
import com.habibah.springapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsable de la gestion des adresses de livraison des utilisateurs.
 * <p>Il permet de créer, consulter, modifier et supprimer les adresses
 * enregistrées par un utilisateur.</p>
 * <p>Il garantit qu'un utilisateur ne possède qu'une seule adresse
 * de livraison par défaut.</p>
 */
@RequiredArgsConstructor
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * Récupère toutes les adresses de l'utilisateur authentifié.
     * @param email adresse e-mail de l'utilisateur
     * @return liste des adresses enregistrées
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(String email) {
        return addressRepository
                .findAllByUserEmailOrderByDefaultAddressDescIdDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Enregistre une nouvelle adresse pour l'utilisateur authentifié.
     * <p>La nouvelle adresse devient l'adresse par défaut uniquement si
     * ce statut est explicitement demandé.</p>
     * @param request informations de la nouvelle adresse
     * @param email adresse e-mail de l'utilisateur
     * @return adresse enregistrée
     */
    @Transactional
    public AddressResponse createAddress(AddressRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (request.defaultAddress()) {
            clearDefaultAddress(email);
        }

        Address address = Address.builder()
                .label(request.label())
                .recipientName(request.recipientName())
                .street(request.street())
                .additionalAddress(request.additionalAddress())
                .postalCode(request.postalCode())
                .city(request.city())
                .country(request.country())
                .phone(request.phone())
                .defaultAddress(request.defaultAddress())
                .user(user)
                .build();

        return toResponse(addressRepository.save(address));
    }

    /**
     * Modifie une adresse appartenant à l'utilisateur authentifié.
     * <p>Le statut par défaut de l'adresse est mis à jour conformément
     * à la valeur transmise dans la requête.</p>
     * @param id identifiant de l'adresse
     * @param request nouvelles informations
     * @param email adresse e-mail de l'utilisateur
     * @return adresse modifiée
     */
    @Transactional
    public AddressResponse updateAddress(
            Long id,
            AddressRequest request,
            String email
    ) {
        Address address = findUserAddress(id, email);

        if (request.defaultAddress()) {
            setDefaultAddressInternal(address, email);
        } else {
            address.setDefaultAddress(false);
        }

        address.setLabel(request.label());
        address.setRecipientName(request.recipientName());
        address.setStreet(request.street());
        address.setAdditionalAddress(request.additionalAddress());
        address.setPostalCode(request.postalCode());
        address.setCity(request.city());
        address.setCountry(request.country());
        address.setPhone(request.phone());

        return toResponse(addressRepository.save(address));
    }

    /**
     * Supprime une adresse appartenant à l'utilisateur authentifié.
     * <p>La suppression d'une adresse par défaut ne provoque pas
     * la désignation automatique d'une autre adresse par défaut.</p>
     * @param id identifiant de l'adresse
     * @param email adresse e-mail de l'utilisateur
     */
    @Transactional
    public void deleteAddress(Long id, String email) {
        Address address = findUserAddress(id, email);

        addressRepository.delete(address);
        addressRepository.flush();
    }

    /**
     * Définit explicitement une adresse comme adresse de livraison par défaut.
     * @param id identifiant de l'adresse
     * @param email adresse e-mail de l'utilisateur
     * @return adresse devenue l'adresse par défaut
     * @throws NotFoundException si l'adresse n'existe pas ou n'appartient pas
     * à l'utilisateur
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long id, String email) {
        Address address = findUserAddress(id, email);
        setDefaultAddressInternal(address, email);
        return toResponse(address);
    }

    /**
     * Convertit une entité Address en AddressResponse.
     * @param address adresse à convertir
     * @return représentation DTO de l'adresse
     */
    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getStreet(),
                address.getAdditionalAddress(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry(),
                address.getPhone(),
                address.isDefaultAddress()
        );
    }

    /**
     * Définit une adresse comme adresse par défaut.
     * <p>Si une autre adresse est actuellement définie par défaut,
     * son statut est retiré avant de définir la nouvelle.</p>
     * @param address adresse à définir par défaut
     * @param email adresse e-mail du propriétaire
     */
    private void setDefaultAddressInternal(Address address, String email) {
        addressRepository.findByUserEmailAndDefaultAddressTrue(email)
                .filter(currentDefault -> !currentDefault.getId().equals(address.getId()))
                .ifPresent(currentDefault -> {
                    currentDefault.setDefaultAddress(false);
                    addressRepository.saveAndFlush(currentDefault);
                });

        address.setDefaultAddress(true);
        addressRepository.saveAndFlush(address);
    }

    /**
     * Retire le statut par défaut de l'adresse actuellement sélectionnée.
     * @param email adresse e-mail de l'utilisateur
     */
    private void clearDefaultAddress(String email) {
        addressRepository.findByUserEmailAndDefaultAddressTrue(email)
                .ifPresent(address -> {
                    address.setDefaultAddress(false);
                    addressRepository.saveAndFlush(address);
                });
    }

    /**
     * Recherche une adresse en vérifiant qu'elle appartient à l'utilisateur.
     * @param id identifiant de l'adresse
     * @param email adresse e-mail du propriétaire
     * @return adresse correspondante
     * @throws NotFoundException si l'adresse est introuvable
     */
    private Address findUserAddress(Long id, String email) {
        return addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Adresse introuvable"));
    }
}