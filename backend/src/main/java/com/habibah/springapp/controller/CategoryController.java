package com.habibah.springapp.controller;

import com.habibah.springapp.dto.CategoryRequest;
import com.habibah.springapp.dto.CategoryResponse;
import com.habibah.springapp.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

/**
 * Contrôleur REST responsable de la gestion des catégories de produits.
 * <p>Expose les endpoints permettant de consulter toutes les catégories,
 * d'en créer une nouvelle, de modifier une catégorie existante
 * et de la supprimer.</p>
 * <p>Les opérations métier sont déléguées à {@link CategoryService}</p>
 */
@Tag(
        name = "Catégories",
        description = "Consultation et gestion des catégories de produits"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    /**
     * Service chargé de la gestion métier des catégories.
     */
    private final CategoryService categoryService;

    /**
     * Récupère l'ensemble des catégories enregistrées.
     * @return la liste des catégories disponibles
     */
    @Operation(
            summary = "Lister les catégories",
            description = "Retourne toutes les catégories disponibles"
    )
    @GetMapping
    public List<CategoryResponse> categories() {
        return categoryService.findAll();
    }

    /**
     * Crée une nouvelle catégorie.
     * <p>Les données reçues sont validées avant leur transmission au service</p>
     * @param request données nécessaires à la création de la catégorie
     * @return la catégorie créée
     */
    @Operation(
            summary = "Créer une catégorie",
            description = "Crée une nouvelle catégorie de produits"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    /**
     * Met à jour une catégorie existante.
     * <p>Les données fournies sont validées avant la mise à jour.</p>
     * @param id identifiant de la catégorie à modifier
     * @param request nouvelles données de la catégorie
     * @return la catégorie mise à jour
     */
    @Operation(
            summary = "Modifier une catégorie",
            description = "Met à jour une catégorie existante à partir de son identifiant"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    /**
     * Supprime une catégorie à partir de son identifiant.
     * @param id identifiant de la catégorie à supprimer
     */
    @Operation(
            summary = "Supprimer une catégorie",
            description = "Supprime une catégorie à partir de son identifiant"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
