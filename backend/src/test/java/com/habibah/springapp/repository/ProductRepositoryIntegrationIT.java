package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Category;
import com.habibah.springapp.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.flyway.enabled=true"
        }
)
public class ProductRepositoryIntegrationIT {
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

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testShouldFindProductsByCategory() {
        Category electronics = createCategory("Electronics");
        Category books = createCategory("Books");
        Product phone = createProduct("iPhone", electronics, new BigDecimal("1400.00"));
        createProduct("Novel", books, new BigDecimal("120.00"));

        productRepository.saveAndFlush(phone);

        List<Product> products = productRepository.findAll(ProductSpecification.hasCategory(electronics.getId()));
        assertThat(products)
                .extracting(Product::getName)
                .containsExactly("iPhone");

    }

    @Test
    void testShouldFindProductsByNameIgnoringCase() {
        createProduct("Book", null, new BigDecimal("120.00"));
        createProduct("Smartphone", null, new BigDecimal("120.00"));
        productRepository.flush();

        List<Product> products = productRepository.findAll(ProductSpecification.nameContains("PHONE"));
        assertThat(products)
                .extracting(Product::getName)
                .containsExactly("Smartphone");
    }

    @Test
    void testShouldFilterProductsWhenCategoryIdIsNull() {
        createProduct("Book", null, new BigDecimal("120.00"));
        createProduct("Smartphone", null, new BigDecimal("120.00"));
        productRepository.flush();

        List<Product> products = productRepository.findAll(ProductSpecification.hasCategory(null));
        assertThat(products).hasSize(2);
    }

    @Test
    void testShouldNotFilterProductsWhenSearchIsBlank() {
        createProduct("Book", null, new BigDecimal("120.00"));
        createProduct("Smartphone", null, new BigDecimal("120.00"));
        productRepository.flush();

        List<Product> products = productRepository.findAll(ProductSpecification.nameContains("     "));
        assertThat(products).hasSize(2);
    }

    @Test
    void testShouldReturnProductsUsingPagination() {
        createProduct("Book", null, new BigDecimal("120.00"));
        createProduct("Smartphone", null, new BigDecimal("20.00"));
        createProduct("Tablet", null, new BigDecimal("220.00"));
        productRepository.flush();

        Page<Product> page = productRepository.findAll((Specification<Product>) null, PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
    }

    private Product createProduct(String name, Category category, BigDecimal price) {
        Product product = Product.builder()
                .name(name)
                .description("Test product")
                .price(price)
                .stock(10)
                .category(category)
                .build();
        return productRepository.save(product);
    }
}
