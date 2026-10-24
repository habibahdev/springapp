package com.habibah.springapp.controller;

import com.habibah.springapp.dto.ProductRequest;
import com.habibah.springapp.dto.ProductResponse;
import com.habibah.springapp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur REST responsable de la gestion des produits.
 * <p>Expose les endpoints permettant de rechercher des produits, de consulter
 * un produit par son identifiant, de créer un produit, de le modifier et de
 * le supprimer.</p>
 * <p>La recherche prend en charge la pagination, le filtrage par catégorie et
 * la recherche textuelle. Le tri est limité aux champs autorisés par le
 * contrôleur.</p>
 * <p>Les opérations métiers sont déléguées à {@link ProductService}</p>
 */
@Tag(
        name = "Produits",
        description = "Recherche, consultation et gestion des produits"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    /**
     * Service chargé de la gestion métier des produits.
     */
    private final ProductService productService;

    /**
     * Recherche les produits en appliquant les filtres et la pagination demandés.
     * <p>La recherche peut être filtrée par identifiant de catégorie et par texte.
     * Les résultats sont paginés et triés selon le champ demandé.</p>
     * <p>La taille de page est limitée à une valeur comprise entre 1 et 50. Le tri
     * est autorisé sur les champs {@code name}, {@code price} et {@code id}. Toute
     * autre valeur entraîne l'utilisation du champ {@code name}</p>
     * @param categoryId identifiant de la catégorie utilisée pour filtrer les produits,
     *                   ou {@code null} pour ne pas appliquer ce filtre
     * @param q texte recherché, ou {@code null} en l'absence de recherche textuelle
     * @param page numéro de la page demandée, égale à 0 par défaut
     * @param size nombre de produits par page, égal à 12 par défaut
     * @param sort champ utilisé pour trier les résultats, égale à {@code name}
     * @return une page contenant les produits correspondant aux critères de recherche
     */
    @Operation(
            summary = "Rechercher les produits",
            description = "Recherche les produits avec des filtres facultatifs, "
            + "une pagination et un tri"
    )
    @GetMapping
    public Page<ProductResponse> products(
            @Parameter(description = "Identifiant de la catégorie")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Texte recherché dans les produits")
            @RequestParam(required = false) String q,
            @Parameter(description = "Numéro de page à partir de zéro")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre de résultats par page, entre 1 et 50")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Champ de tri : name, price ou id")
            @RequestParam(defaultValue = "name") String sort
    ) {
        int safeSize = Math.clamp(size, 1, 50);
        String safeSort = switch (sort) {
            case "price", "id" -> sort;
            default -> "name";
        };
        return productService.search(categoryId, q, PageRequest.of(page, safeSize, Sort.by(safeSort)));
    }

    /**
     * Récupère un produit à partir de son identifiant
     * @param id identifiant du produit recherché
     * @return les infos du produit correspondant
     */
    @Operation(
            summary = "Consulter un produit",
            description = "Récupère un produit par son identifiant"
    )
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.findById(id);
    }

    /**
     * Crée un nouveau produit.
     * <p>Les données reçues dans le corps de la requête sont validées
     * avant leur transmission au service.</p>
     * @param request données nécessaires à la création du produit
     * @return le produit créé
     */
    @Operation(
            summary = "Créer un produit",
            description = "Enregistre un nouveau produit"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /**
     * Met à jour un produit existant.
     * <p>Les infos fournies dans la requête sont validées
     * avant la mise à jour.</p>
     * @param id identifiant du produit à modifier
     * @param request nouvelles données du produit
     * @return le produit mis à jour
     */
    @Operation(
            summary = "Modifier un produit",
            description = "Met à jour un produit existant"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    /**
     * Supprime un produit à partir de son identifiant.
     * @param id identifiant du produit à supprimer
     */
    @Operation(
            summary = "Supprimer un produit",
            description = "Supprime un produit par son identifiant"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
