package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository permettant de gérer la persistance des produits et d'effectuer
 * des recherches dynamiques dans le catalogue.
 * <p>Cette interface fournit les opérations CRUD sur l'entité {@link Product},
 * ainsi que les fonctionnalités de pagination et de tri proposées par Spring data JPA</p>
 *
 * @see Product
 * @see ProductSpecification
 * @see JpaRepository
 * @see JpaSpecificationExecutor
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    /**
     * Recherche les produits correspondant à une spécification donnée et
     * retourne les résultats sous forme de page.
     * <p>La spécification permet de définir les critères de filtrage
     * applicables aux produits. L'objet {@link Pageable} détermine
     * notamment le numéro de page, la taille de page et l'ordre de tri</p>
     * <p>Si la spécification est {@code null}, aucun critère de filtrage
     * supplémentaire n'est demandé</p>
     *
     * @param spec spécification définissant les critères de recherche,
     *             ou {@code null} pour rechercher sans filtre.
     * @param pageable paramètre de pagination et de tri
     * @return une page contenant les produits correspondant aux critères, accompagnée
     * des informations de pagination
     */
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
