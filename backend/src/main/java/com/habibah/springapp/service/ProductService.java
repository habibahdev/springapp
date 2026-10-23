package com.habibah.springapp.service;

import com.habibah.springapp.dto.ProductRequest;
import com.habibah.springapp.dto.ProductResponse;
import com.habibah.springapp.entity.Category;
import com.habibah.springapp.entity.Product;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.CategoryRepository;
import com.habibah.springapp.repository.ProductRepository;
import com.habibah.springapp.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier chargé de la gestion du catalogue des produits.
 * <p>Il permet de rechercher les produits avec des filtres et une pagination,
 * de consulter un produit par son identifiant et d'effectuer les opérations
 * de création, de modification et de suppression.</p>
 * <p>Il assure également l'association des produits à leurs catégories
 * et la conversion des entités en dto destinés à l'API.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Recherche les produits selon une catégorie et un texte facultatifs,
     * avec pagination et tri définis par le paramètre {@code pageable}
     * @param categoryId identifiant de la catégorie recherchée, ou {@code null}
     *                   pour ne pas filtrer par catégorie
     * @param q texte recherché dans le nom des produits, ou {@code null} pour
     *          ne pas appliquer ce filtre
     * @param pageable paramètres de pagination et de tri
     * @return page de produits correspondant aux critères, convertis en dto
     */
    public Page<ProductResponse> search(Long categoryId, String q, Pageable pageable) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecification.hasCategory(categoryId),
                ProductSpecification.nameContains(q)
        );
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    /**
     * Récupère un produit à partir de son identifiant.
     * @param id identifiant du produit recherché
     * @return produit trouvé sous forme de dto
     */
    public ProductResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /**
     * Crée un produit à partir des données fournies.
     * <p>LEs propriétés du produit et sa catégorie éventuelle sont définies
     * avant son enregistrement en base de données.</p>
     * @param request données nécessaires à la création du produit
     * @return produit créé sous forme de dto
     */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    /**
     * Modifie un produit existant à partir des nouvelles données.
     * @param id identifiant du produit à modifier
     * @param request nouvelles données eu produit
     * @return produit modifié sous forme de dto
     */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        apply(product, request);
        return toResponse(product);
    }

    /**
     * Supprime un produit existant.
     * @param id identifiant du produit à supprimer
     */
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Produit introuvable " + id);
        }
        productRepository.deleteById(id);
        productRepository.flush();
    }

    /**
     * Applique les données d'une requête à une entité produit.
     * <p>Si un identifiant de catégorie est fourni, la catégorie
     * correspondante est recherchée et associée au produit. Si aucun
     * identifiant n'est fourni, la catégorie du produit est supprimée.</p>
     * @param product entité produit à renseigner
     * @param r données à appliquer au produit
     */
    private void apply(Product product, ProductRequest r) {
        product.setName(r.name());
        product.setDescription(r.description());
        product.setPrice(r.price());
        product.setStock(r.stock());
        product.setImageUrl(r.imageUrl());
        if (r.categoryId() != null) {
            Category category = categoryRepository.findById(r.categoryId())
                    .orElseThrow(() -> new NotFoundException("Catégorie introuvable " + r.categoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
    }

    /**
     * Convertit une entité produit en dto de réponse.
     * <p>Les infos de la catégorie associée sont incluses lorsqu'une
     * catégorie existe.</p>
     * @param p entité produit à convertir
     * @return dto content les infos du produit et de sa catégorie
     */
    private ProductResponse toResponse(Product p) {
        Category c = p.getCategory();
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(), p.getImageUrl(),
                c != null ? c.getId() : null,
                c != null ? c.getName() : null
        );
    }

    /**
     * Recherche un produit ou déclecnhe une exception s'il n'existe pas/
     * @param id identifiant du produit recherché
     * @return entité produit trouvée
     */
    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produit introuvable " + id));
    }
}
