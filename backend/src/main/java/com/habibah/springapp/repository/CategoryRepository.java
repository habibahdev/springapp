package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository permettant de gérer la persistence des catégories.
 *
 * <p>Cette interface fournit les opérations CRUD sur les entités {@link Category}</p>
 *
 * <p>Les requêtes CRUD sont générées automatiquement par Spring Data JPA.</p>
 *
 * @see Category
 * @see JpaRepository
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
