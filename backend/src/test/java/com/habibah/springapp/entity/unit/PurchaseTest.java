package com.habibah.springapp.entity.unit;

import com.habibah.springapp.entity.Detail;
import com.habibah.springapp.entity.Product;
import com.habibah.springapp.entity.Purchase;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseTest {
    @Test
    void testShouldCreatePurchaseWithBuilder() {
        LocalDateTime creationDate = LocalDateTime.of(2026, 10, 20, 10, 30);
        Purchase purchase = Purchase.builder()
                .id(1L)
                .customerName("Jane Doe")
                .customerEmail("janedoe@example.com")
                .shippingAddress("123 Shipping Address")
                .status(PurchaseStatus.PENDING)
                .createdAt(creationDate)
                .total(new BigDecimal("99.99"))
                .build();

        assertEquals(1L, purchase.getId());
        assertEquals("Jane Doe", purchase.getCustomerName());
        assertEquals("janedoe@example.com", purchase.getCustomerEmail());
        assertEquals("123 Shipping Address", purchase.getShippingAddress());
        assertEquals(PurchaseStatus.PENDING, purchase.getStatus());
        assertEquals(creationDate, purchase.getCreatedAt());
        assertEquals(0, new BigDecimal("99.99").compareTo(purchase.getTotal()));
        assertNotNull(purchase.getItems());
        assertTrue(purchase.getItems().isEmpty());
    }

    @Test
    void testShouldCreatePurchaseWithNoArgsConstructor() {
        Purchase purchase = new Purchase();

        assertNull(purchase.getId());
        assertNotNull(purchase.getCreatedAt());
        assertNull(purchase.getShippingAddress());
        assertNull(purchase.getCustomerEmail());
        assertNull(purchase.getCustomerName());
        assertNotNull(purchase.getItems());
        assertEquals(PurchaseStatus.PENDING, purchase.getStatus());
        assertTrue(purchase.getItems().isEmpty());
        assertNull(purchase.getTotal());
    }

    @Test
    void testShouldApplyDefaultValuesWhenUsingBuilder() {
        Purchase purchase = Purchase.builder().build();

        assertEquals(PurchaseStatus.PENDING, purchase.getStatus());
        assertNotNull(purchase.getItems());
        assertTrue(purchase.getItems().isEmpty());
        assertNotNull(purchase.getCreatedAt());
    }

    @Test
    void testShouldUpdatePurchaseFields() {
        Purchase purchase = new Purchase();

        purchase.setCustomerEmail("newemail@example.com");
        purchase.setCustomerName("Jane Smith");
        purchase.setShippingAddress("Shipping");
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotal(new BigDecimal("167.90"));

        assertEquals("Jane Smith", purchase.getCustomerName());
        assertEquals("newemail@example.com", purchase.getCustomerEmail());
        assertEquals("Shipping", purchase.getShippingAddress());
        assertEquals(PurchaseStatus.PAID, purchase.getStatus());
        assertEquals(0, new BigDecimal("167.90").compareTo(purchase.getTotal()));
    }

    @Test
    void testShouldAddItemToPurchaseAndSetPurchaseReference() {
        Purchase purchase = Purchase.builder()
                .id(1L)
                .build();

        Product product = Product.builder()
                .id(2L)
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        Detail detail = Detail.builder()
                .id(12L)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();

        purchase.addItem(detail);

        assertEquals(1, purchase.getItems().size());
        assertSame(detail, purchase.getItems().getFirst());
        assertSame(purchase, detail.getPurchase());
        assertSame(product, detail.getProduct());
    }

    @Test
    void testShouldAddMultipleItemsToPurchase() {
        Purchase purchase = new Purchase();

        Detail item1 = Detail.builder()
                .quantity(1)
                .unitPrice(new BigDecimal("15.00"))
                .build();

        Detail item2 = Detail.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        purchase.addItem(item1);
        purchase.addItem(item2);

        List<Detail> items = purchase.getItems();

        assertEquals(2, items.size());
        assertSame(item1, items.get(0));
        assertSame(item2, items.get(1));
        assertSame(purchase, item1.getPurchase());
        assertSame(purchase, item2.getPurchase());
    }
}
