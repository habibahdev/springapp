package com.habibah.springapp.service.assistant;

import com.habibah.springapp.dto.assistant.ProductSearchCriteria;
import com.habibah.springapp.dto.assistant.RecommendedProduct;
import com.habibah.springapp.entity.Product;
import com.habibah.springapp.repository.ProductRepository;
import com.habibah.springapp.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service chargé de recherche les produits correspondant
 * aux critères extrait des demandes des clients
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductSearchService {

    private static final int MAX_RESULTS = 5;

    private final ProductRepository productRepository;

    /**
     * Recherche les produits correspondant aux critères fournir
     * @param criteria critère de recherche
     * @return liste des produits correspondants limitée à 5 résultats
     */
    public List<RecommendedProduct> search(ProductSearchCriteria criteria) {
        Specification<Product> specification = buildSpecification(criteria);
        PageRequest pageable = PageRequest.of(0, MAX_RESULTS, Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> products = productRepository.findAll(specification, pageable);
        return products.getContent().stream().map(this::toRecommendedProduct).toList();
    }

    /**
     * Construit une spécification jpa à partir des critères de recherche
     * @param criteria critères fournis par le client
     * @return spécification regroupant les filtres applicables
     */
    private Specification<Product> buildSpecification(ProductSearchCriteria criteria) {
        List<Specification<Product>> specifications = new ArrayList<>();
        if (criteria == null) {
            return Specification.allOf(specifications);
        }
        if (criteria.keywords() != null) {
            criteria.keywords().stream()
                    .filter(keyword -> keyword != null && !keyword.isBlank())
                    .map(ProductSpecification::nameOrDescriptionContains)
                    .forEach(specifications::add);
        }
        specifications.add(ProductSpecification.priceGreaterThanEqualOrEqualTo(criteria.minPrice()));
        specifications.add(ProductSpecification.priceLessThanEqualOrEqualTo(criteria.maxPrice()));
        specifications.add(ProductSpecification.hasCategory(criteria.categoryId()));
        if (criteria.onlyAvailable()) {
            specifications.add(ProductSpecification.isAvailable());
        }
        return Specification.allOf(specifications);
    }

    /**
     * Convertir une entité produit en dto destinée à l'assistant
     * @param product produit à convertir
     * @return dto contenant les infos pour le chatbot
     */
    private RecommendedProduct toRecommendedProduct(Product product) {
        return new RecommendedProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock() > 0,
                product.getImageUrl(),
                product.getCategory() != null ? product.getCategory().getName() : null
        );
    }
}
