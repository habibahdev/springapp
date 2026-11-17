package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository permettant de gérer la persistance des adresses de livraisons
 * <p>Les méthodes de recherche prennent en compte l'utilisateur propriétaire
 * afin de limiter les opérations aux adresses de l'utilisateur authentifié.</p>
 *
 * @see Address
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
    /**
     * Récupère les adresses d'un utilisateur.
     * <p>Les adresses par défaut sont placées en premier.</p>
     * @param email adresse e-mail de l'utilisateur
     * @return liste des adresses de l'utilisateur
     */
    List<Address> findAllByUserEmailOrderByDefaultAddressDescIdDesc(String email);

    /**
     * Recherche une adresse appartenant à un utilisateur précis.
     * @param id identifiant de l'adresse
     * @param email adresse e-mail du propriétaire
     * @return adresse correspondante, si elle existe
     */
    Optional<Address> findByIdAndUserEmail(Long id, String email);

    /**
     * Vérifie si un utilisateur possède déjà une adresse par défaut.
     * @param email adresse e-mail de l'utilisateur
     * @return true si une adresse par défaut existe
     */
    boolean existsByUserEmailAndDefaultAddressTrue(String email);

    /**
     * Recherche l'adresse par défaut d'un utilisateur
     * @param email adresse e-mail de l'utilisateur
     * @return adresse par défaut, si elle existe
     */
    Optional<Address> findByUserEmailAndDefaultAddressTrue(String email);
}
