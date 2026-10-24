package com.habibah.springapp.controller;

import com.habibah.springapp.dto.CategoryRequest;
import com.habibah.springapp.dto.CategoryResponse;
import com.habibah.springapp.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void testShouldReturnAllCategories() {
        List<CategoryResponse> expected = List.of(
                new CategoryResponse(1L, "Informatique"),
                new CategoryResponse(2L, "Accessoires")
        );

        when(categoryService.findAll()).thenReturn(expected);

        List<CategoryResponse> result = categoryController.categories();

        assertEquals(2, result.size());
        assertEquals(expected, result);
        verify(categoryService).findAll();
    }

    @Test
    void testShouldDelegateCreateCategoryToCategoryService() {
        CategoryRequest request = new CategoryRequest("Informatique");
        CategoryResponse expected = new CategoryResponse(1L, "Informatique");

        when(categoryService.create(request)).thenReturn(expected);

        CategoryResponse result = categoryController.create(request);

        assertEquals(expected, result);
        verify(categoryService).create(request);
    }

    @Test
    void testShouldDelegateUpdateCategoryToCategoryService() {
        CategoryRequest request = new CategoryRequest("Accessoires");
        CategoryResponse expected = new CategoryResponse(1L, "Accessoires");

        when(categoryService.update(1L, request)).thenReturn(expected);

        CategoryResponse result = categoryController.update(1L, request);

        assertEquals(expected, result);
        verify(categoryService).update(1L, request);
    }

    @Test
    void testShouldDelegateDeleteCategoryToCategoryService() {
        categoryController.delete(1L);

        verify(categoryService).delete(1L);
    }
}
