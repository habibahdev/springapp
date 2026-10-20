package com.habibah.springapp.entity.unit;

import com.habibah.springapp.entity.Detail;
import com.habibah.springapp.entity.Product;
import com.habibah.springapp.entity.Purchase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DetailTest {
    @Test
    void testShouldCreateDetailWithBuilder() {
        Purchase purchase = Purchase.builder()
                .id(1L)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        Detail detail = Detail.builder()
                .id(2L)
                .purchase(purchase)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();

        assertEquals(2L, detail.getId());
        assertSame(purchase, detail.getPurchase());
        assertSame(product, detail.getProduct());
        assertEquals(2, detail.getQuantity());
        assertEquals(0, new BigDecimal("99.99").compareTo(detail.getUnitPrice()));
    }

    @Test
    void testShouldCreateDetailWithAllArgsConstructor() {
        Purchase purchase = new Purchase();

        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("15.00"))
                .stock(10)
                .build();

        Detail detail = new Detail(
                2L,
                purchase,
                product,
                3,
                new BigDecimal("15.00")
        );

        assertEquals(2L, detail.getId());
        assertSame(purchase, detail.getPurchase());
        assertSame(product, detail.getProduct());
        assertEquals(3, detail.getQuantity());
        assertEquals(0, new BigDecimal("15.00").compareTo(detail.getUnitPrice()));
    }

    @Test
    void testShouldCreateEmptyDetailWithNoArgsConstructor() {
        Detail detail = new Detail();

        assertNull(detail.getId());
        assertNull(detail.getPurchase());
        assertNull(detail.getProduct());
        assertEquals(0, detail.getQuantity());
        assertNull(detail.getUnitPrice());
    }

    @Test
    void testShouldUpdateDetailFields() {
        Detail detail = new Detail();

        Purchase purchase = new Purchase();

        Product product = Product.builder()
                .id(1L)
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        detail.setPurchase(purchase);
        detail.setProduct(product);
        detail.setQuantity(5);
        detail.setUnitPrice(new BigDecimal("12.50"));

        assertSame(purchase, detail.getPurchase());
        assertSame(product, detail.getProduct());
        assertEquals(5, detail.getQuantity());
        assertEquals(0, new BigDecimal("12.50").compareTo(detail.getUnitPrice()));
    }
}
