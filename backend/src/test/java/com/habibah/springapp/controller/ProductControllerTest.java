package com.habibah.springapp.controller;

import com.habibah.springapp.dto.ProductRequest;
import com.habibah.springapp.dto.ProductResponse;
import com.habibah.springapp.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void testShouldReturnPaginatedResults() {
        ProductResponse product = createProductResponse();
        Page<ProductResponse> expectedPage = new PageImpl<>(
                List.of(product),
                PageRequest.of(0, 12, Sort.by("name")),
                1
        );
        when(productService.search(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<ProductResponse> result = productController.products(null, null, 0, 12, "name");
        assertEquals(1, result.getTotalElements());
        assertEquals(product, result.getContent().getFirst());

        verify(productService).search(
                isNull(),
                isNull(),
                argThat(pageable ->
                        pageable.getPageNumber() == 0
                && pageable.getPageSize() == 12
                && pageable.getSort().getOrderFor("name") != null)
        );
    }

    @Test
    void testShouldApplyFiltersAndAllowedSort() {
        Page<ProductResponse> expectedPage = new PageImpl<>(
                List.of(createProductResponse())
        );
        when(productService.search(
                eq(2L),
                eq("clavier"),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<ProductResponse> result = productController.products(2L, "clavier", 1, 20, "price");
        assertEquals(1, result.getTotalElements());
        verify(productService).search(
                eq(2L),
                eq("clavier"),
                argThat(pageable -> pageable.getPageNumber() == 1
                && pageable.getPageSize() == 20
                && pageable.getSort().getOrderFor("price") != null)
        );
    }

    @Test
    void testShouldLimitPageSizeToMaximumOfFifty() {
        when(productService.search(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());
        productController.products(null, null, 0, 100, "name");
        verify(productService).search(
                isNull(),
                isNull(),
                argThat(pageable -> pageable.getPageSize() == 50)
        );
    }

    @Test
    void testShouldLimitPageSizeToMinimumOfOne() {
        when(productService.search(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());
        productController.products(null, null, 0, 0, "name");
        verify(productService).search(
                isNull(),
                isNull(),
                argThat(pageable -> pageable.getPageSize() == 1)
        );
    }

    @Test
    void testShouldReturnProductById() {
        ProductResponse product = createProductResponse();

        when(productService.findById(1L)).thenReturn(product);

        ProductResponse result = productController.get(1L);

        assertEquals(product, result);
        verify(productService).findById(1L);
    }

    @Test
    void testShouldDelegateCreateProductToProductService() {
        ProductRequest request = new ProductRequest(
                "Clavier",
                "Clavier mécanique",
                new BigDecimal("50.00"),
                10,
                "https://example.com/clavier.jpg",
                2L
        );

        ProductResponse expected = createProductResponse();

        when(productService.create(request)).thenReturn(expected);

        ProductResponse result = productController.create(request);

        assertEquals(expected, result);
        verify(productService).create(request);
    }

    @Test
    void testShouldDelegateUpdateProductToProductService() {
        ProductRequest request = new ProductRequest(
                "Clavier modifié",
                "Nouveau clavier",
                new BigDecimal("60.00"),
                5,
                "https://example.com/clavier.jpg",
                2L
        );

        ProductResponse expected = createProductResponse();

        when(productService.update(1L, request)).thenReturn(expected);

        ProductResponse result = productController.update(1L, request);

        assertEquals(expected, result);
        verify(productService).update(1L, request);
    }

    @Test
    void testShouldDelegateDeleteProductToProductService() {
        doNothing().when(productService).delete(1L);

        productController.delete(1L);

        verify(productService).delete(1L);
    }

    private ProductResponse createProductResponse() {
        return new ProductResponse(
                1L,
                "Clavier",
                "Clavier mécnique",
                new BigDecimal("50.00"),
                10,
                "http://example.com/clavier.jpg",
                2L,
                "Informatique"
        );
    }
}
