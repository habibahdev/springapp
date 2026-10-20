package com.habibah.springapp.entity.unit;

import com.habibah.springapp.entity.Category;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {
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
    void testShouldCreateCategoryWithBuilder() {
        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        assertEquals(1L, category.getId());
        assertEquals("Category", category.getName());
    }

    @Test
    void testShouldCreateCategoryWithAllArgsConstructor() {
        Category category = new Category(1L, "Category");

        assertEquals(1L, category.getId());
        assertEquals("Category", category.getName());
    }

    @Test
    void testShouldCreateEmptyCategoryWithNoArgsConstructor() {
        Category category = new Category();

        assertNull(category.getId());
        assertNull(category.getName());
    }

    @Test
    void testShouldUpdateCategoryName() {
        Category category = Category.builder()
                .name("Category")
                .build();

        category.setName("Cat");

        assertEquals("Cat", category.getName());
    }

    @Test
    void testShouldAcceptValidCategoryName() {
        Category category = Category.builder()
                .name("Electronics")
                .build();

        assertTrue(validator.validate(category).isEmpty());
    }

    @Test
    void testShouldRejectBlankCategoryName() {
        Category category = Category.builder()
                .name(" ")
                .build();

        assertFalse(validator.validate(category).isEmpty());
    }
}
