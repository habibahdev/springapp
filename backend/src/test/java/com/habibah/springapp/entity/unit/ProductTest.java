package com.habibah.springapp.entity.unit;

import com.habibah.springapp.entity.Category;
import com.habibah.springapp.entity.Product;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void testShouldCreateProductWithBuilder() {
        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Product")
                .description("Product's description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .imageUrl("image.png")
                .category(category)
                .build();

        assertEquals(1L, product.getId());
        assertEquals("Product", product.getName());
        assertEquals("Product's description", product.getDescription());
        assertEquals(0, new BigDecimal("99.99").compareTo(product.getPrice()));
        assertEquals("image.png", product.getImageUrl());
        assertEquals(category, product.getCategory());
        assertEquals(10, product.getStock());
    }

    @Test
    void testShouldCreateEmptyProductWithNoArgsConstructor() {
        Product product = new Product();

        assertNull(product.getId());
        assertNull(product.getName());
        assertNull(product.getDescription());
        assertNull(product.getImageUrl());
        assertEquals(0, product.getStock());
        assertNull(product.getCategory());
        assertNull(product.getPrice());
    }

    @Test
    void testShouldCreateProductWithAllArgsConstructor() {
        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        Product product = new Product(
                1L,
                "Product",
                "Product's description",
                new BigDecimal("99.99"),
                10,
                "image.png",
                category
        );

        assertEquals("Product", product.getName());
        assertEquals("Product's description", product.getDescription());
        assertEquals(10, product.getStock());
    }

    @Test
    void testShouldUpdateProductFields() {
        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        product.setName("New name");
        product.setPrice(new BigDecimal("999.99"));
        product.setStock(3);

        assertEquals("New name", product.getName());
        assertEquals(0, new BigDecimal("999.99").compareTo(product.getPrice()));
        assertEquals(3, product.getStock());
    }

    @Test
    void testShouldAcceptValidProduct() {
        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        assertTrue(validator.validate(product).isEmpty());
    }

    @Test
    void testShouldRejectBlankProductName() {
        Product product = Product.builder()
                .name(" ")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        Set<String> violations = validator.validate(product)
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(violations.contains("name"));
    }

    @Test
    void testShouldRejectNullPrice() {
        Product product = Product.builder()
                .name("Product")
                .price(null)
                .stock(10)
                .build();

        Set<String> violations = validator.validate(product)
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(violations.contains("price"));
    }

    @Test
    void tesShouldRejectZeroPrice() {
        Product product = Product.builder()
                .name("Product")
                .price(BigDecimal.ZERO)
                .stock(10)
                .build();

        assertFalse(validator.validate(product).isEmpty());
    }

    @Test
    void testShouldRejectNegativePrice() {
        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("-99.99"))
                .stock(10)
                .build();

        assertFalse(validator.validate(product).isEmpty());
    }

    @Test
    void testShouldRejectNegativeStock() {
        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(-10)
                .build();

        Set<String> violations = validator.validate(product)
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(violations.contains("stock"));
    }
}
