package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.flyway.enabled=true"
        }
)
public class CategoryRepositoryIntegrationIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("springapp_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testShouldSaveEndRetrieveCategory() {
        Category category = new Category();
        category.setName("Category");
        Category savedCategory = categoryRepository.saveAndFlush(category);

        assertThat(savedCategory.getId()).isNotNull();

        var result = categoryRepository.findById(savedCategory.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Category");
    }

    @Test
    void testShouldReturnAllSavedCategories() {
        Category cat1 = new Category();
        cat1.setName("Category 1");

        Category cat2 = new Category();
        cat2.setName("Category 2");

        categoryRepository.save(cat1);
        categoryRepository.saveAndFlush(cat2);

        assertThat(categoryRepository.findAll())
                .extracting(Category::getName)
                .contains("Category 1", "Category 2");
    }

    @Test
    void testShouldDeleteCategory() {
        Category category = new Category();
        category.setName("Category");

        Category savedCategory = categoryRepository.saveAndFlush(category);

        Long categoryId = savedCategory.getId();
        categoryRepository.deleteById(categoryId);
        categoryRepository.flush();

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }
}
