package com.habibah.springapp.service;

import com.habibah.springapp.dto.CategoryRequest;
import com.habibah.springapp.dto.CategoryResponse;
import com.habibah.springapp.entity.Category;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service métier chargé de gérer les catégories de produits.
 * <p>Il assure les opérations de consultation, de création, de
 * modification et de suppression des catégories.</p>
 * <p>Les opérations de lecture sont effectuées en lecture seule par défaut.
 * Les méthodes qui modifient les données utilisent une transaction d'écriture</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Récupére toutes les catégories et les convertit en dto de réponse.
     * @return liste des catégories disponibles
     */
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Crée une catégorie à partir des données fournies.
     * @param request données nécessaires à la création de la catégorie.
     * @return catégorie créée sous forme de dto
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder().name(request.name()).build();
        return toResponse(categoryRepository.save(category));
    }

    /**
     * Modifie le nom d'une catégorie existante.
     * @param id identifiant de la catégorie à modifier
     * @param request nouvelles données de la catégorie
     * @return catégorie modifiée sous forme de dto
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Catégorie introuvable " + id));
        category.setName(request.name());
        return toResponse(category);
    }

    /**
     * Supprime une catégorie existante.
     * <p>La suppression est précédée d'une vérification de l'existence de
     * la catégorie. Le {@code flush()} force ensuite la synchronisation
     * des changements en attente avec la base de données.</p>
     * @param id identifiant de la catégorie à supprimer
     */
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Catégorie introuvable " + id);
        }
        categoryRepository.deleteById(id);
        categoryRepository.flush();
    }

    /**
     * Convertit une entité catégorie en dto de réponse.
     * @param category entité à convertir
     * @return dto contenant l'identifiant et le nom de la catégorie
     */
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
