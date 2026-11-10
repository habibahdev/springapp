package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Classe utilitaire permettant de construire des critères de recherche
 * dynamique pour les produits.
 * <p>Les spécifications définies dans cette classe peuvent être utilisées
 * avec un repository prenant en charge les {@link Specification},
 * afin de filtrer les produits selon différents critères.</p>
 * <p>Chaque méthode retourne une spécification indépendante. Plusieurs
 * spécifications peuvent être combinées avec les opérations logiques
 * proposées par Spring Data JPA, telles que {@code and()} et {@code or()}</p>
 * <p>Cette classe ne peut pas être instanciée, car elle ne contient que des
 * méthodes utilitaires statiques</p>
 *
 * @see Product
 * @see Specification
 */
public class ProductSpecification {
    /**
     * Constructeur privé empêchant l'instanciation de cette classe.
     */
    private ProductSpecification() {}

    /**
     * Construit un critère de recherche filtrant les produits par catégorie.
     * <p>Si l'identifiant de catégorie est {@code null}, aucune condition
     * de filtrage n'est appliquée. Dans le cas contraire, seuls les produits
     * associés à la catégorie dont l'identifiant correspond sont retenus.</p>
     *
     * @param categoryId identifiant de la catégorie recherchée, ou {@code null}
     *                   si aucun filtrage par catégorie ne doit être appliqué
     * @return une spécification filtrant les produits par identifiant de catégorie,
     * ou {@code null} si aucun filtre n'est demandé
     */
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Construit un critère de recherche recherchant les produits dont le nom
     * contient le texte fourni, sans tenir compte de la casse.
     * <p>Les espaces situés au début et à la fin du texte de recherche sont
     * supprimés. La recherche est insensible à la casse grâce à l'utilisation
     * de la fonction SQL {@code lower()}</p>
     * <p>Les caractères {@code \}, {@code %} et {@code _} sont échappés
     * afin que les caractères génériques SQL présents dans la recherche soient
     * interprétés comme des caractères littéraux</p>
     * @param q texte recherché dans le nom du produit, ou {@code null} si aucun
     *          filtrage par nom n'est demandé
     * @return une spécification filtrant les produits par nom, ou {@code null}
     * si aucun filtrage n'est demandé
     */
    public static Specification<Product> nameContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + escapeLike(q.trim().toLowerCase()) + "%";
            return cb.like(cb.lower(root.get("name")), pattern, '\\');
        };
    }

    /**
     * Recherche les produits dont le nom ou la description contient le mot-clé
     * défini, sans tenir compte de la casse
     * @param keyword mot-clé recherché
     * @return spécification correspondant au non ou à la description
     */
    public static Specification<Product> nameOrDescriptionContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + escapeLike(keyword.trim().toLowerCase()) + "%";
            Predicate nameMatches = cb.like(cb.lower(root.get("name")), pattern, '\\');
            Predicate descriptionMatches = cb.like(cb.lower(root.get("description")), pattern, '\\');
            return cb.or(nameMatches, descriptionMatches);
        };
    }

    /**
     * Filtre les produits dont le pris est supérieur ou égale au prix minimum
     * @param minPrice prix minimum, ou {@code null} pour ne pas application ce filtre
     * @return spécification correspondant au prix minimum
     */
    public static Specification<Product> priceGreaterThanEqualOrEqualTo(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    /**
     * Filtre les produits dont le prix est inférieur ou égal au prix maximum
     * @param maxPrice prix maximum, ou {@code null} pour ne pas application ce filtre
     * @return spécification correspondant au prix maximal
     */
    public static Specification<Product> priceLessThanEqualOrEqualTo(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    /**
     * Conserve uniquement les produits dont le stock est supérieur à zéro
     * @return spécification correspondant aux produits disponibles
     */
    public static Specification<Product> isAvailable() {
        return (root, query, cb) -> cb.greaterThan(root.get("stock"), 0);
    }

    /**
     * Échappe les caractères spéciaux utilisés par l'opérateur sql like
     * @param value texte à échapper
     * @return texte échappé
     */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
