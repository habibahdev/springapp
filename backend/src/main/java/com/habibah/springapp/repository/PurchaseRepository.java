package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository permettant de gérer la persistance des achats.
 * <p>Cette interface fournit les opérations CRUD sur les entités
 * {@link Purchase} grâce à Spring Data JPA.</p>
 * <p>Elle définit également une méthode de recherche permettant de récupérer
 * les achats associés à l'adresse e-mail d'un utilisateur, triès par date
 * de création décroissante.</p>
 *
 * @see Purchase
 * @see JpaRepository
 */
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    /**
     *
     * @param email adresse e-mail de l'utilisateur dont les achats doivent
     *              être récupérés
     * @return liste des achats associés à cette adresse e-mail, triès par date
     * de création décroissante ; liste vide si aucun achat ne correspond
     */
    List<Purchase> findByUserEmailOrderByCreatedAtDesc(String email);
}
