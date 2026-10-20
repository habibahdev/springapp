package com.habibah.springapp.entity.integration;

import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.Product;
import com.habibah.springapp.entity.Purchase;
import com.habibah.springapp.entity.Detail;
import com.habibah.springapp.entity.Category;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import com.habibah.springapp.entity.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
public class EntityIntegrationIT {
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
    private EntityManager entityManager;

    @Test
    void testUserShouldPersistAndRetrieveUser() {
        User user = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        entityManager.persist(user);
        entityManager.flush();

        Long userId = user.getId();
        entityManager.clear();

        User found = entityManager.find(User.class, userId);

        assertNotNull(found);
        assertEquals("janedoe@example.com", found.getEmail());
        assertEquals("Jane Doe", found.getFullName());
        assertEquals("password", found.getPassword());
        assertEquals(Role.USER, found.getRole());
    }

    @Test
    void testUserShouldRejectDuplicatedUserEmail() {
        User user = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password1")
                .role(Role.USER)
                .build();

        User anotherUser = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password2")
                .role(Role.USER)
                .build();

        entityManager.persist(user);
        entityManager.flush();



        assertThrows(PersistenceException.class, () -> {
                entityManager.flush();
                entityManager.persist(anotherUser);
        });
    }

    @Test
    void testCategoryShouldPersistAndRetrieveCategory() {
        Category category = Category.builder()
                        .name("Category")
                                .build();

        entityManager.persist(category);
        entityManager.flush();

        Long categoryId = category.getId();
        entityManager.clear();

        Category found = entityManager.find(Category.class, categoryId);

        assertNotNull(found);
        assertEquals("Category", found.getName());
    }

    @Test
    void testCategoryShouldRejectDuplicatedCategoryName() {
        Category category = Category.builder()
                .name("Category")
                .build();

        Category anotherCategory = Category.builder()
                .name("Category")
                .build();

        entityManager.persist(category);
        entityManager.flush();

        entityManager.persist(anotherCategory);

        assertThrows(PersistenceException.class, () -> entityManager.flush());
    }

    @Test
    void testProductShouldPersistAndRetrieveProductWithCategory() {
        Category category = Category.builder()
                .name("Category")
                .build();

        entityManager.persist(category);

        Product product = Product.builder()
                .name("Product")
                .description("Product's description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .imageUrl("image.png")
                .category(category)
                .build();

        entityManager.persist(product);
        entityManager.flush();

        Long productId = product.getId();
        Long categoryId = category.getId();

        entityManager.clear();

        Product found = entityManager.find(Product.class, productId);

        assertNotNull(found);
        assertEquals("Product", found.getName());
        assertEquals(0, new BigDecimal("99.99").compareTo(found.getPrice()));
        assertEquals(10, found.getStock());
        assertNotNull(found.getCategory());
        assertEquals(categoryId, found.getCategory().getId());
    }

    @Test
    void testPurchaseShouldPersistAndRetrievePurchase() {
        Purchase purchase = Purchase.builder()
                .customerEmail("janedoe@example.com")
                .customerName("Jane Doe")
                .shippingAddress("123 Shipping Address")
                .status(PurchaseStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 10, 20, 12, 0))
                .total(new BigDecimal("99.99"))
                .build();

        entityManager.persist(purchase);
        entityManager.flush();

        Long purchaseId = purchase.getId();

        entityManager.clear();

        Purchase found = entityManager.find(Purchase.class, purchaseId);

        assertNotNull(found);
        assertEquals("Jane Doe", found.getCustomerName());
        assertEquals("jane@example.com", found.getCustomerEmail());
        assertEquals("123 Shipping Address", found.getShippingAddress());
        assertEquals(PurchaseStatus.PENDING, found.getStatus());
        assertEquals(0, new BigDecimal("99.99").compareTo(found.getTotal()));
    }

    @Test
    void testDetailShouldPersistDetailWithPurchaseAndProduct() {
        Category category = Category.builder()
                .name("Category")
                .build();

        entityManager.persist(category);

        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category(category)
                .build();

        entityManager.persist(product);

        Purchase purchase = Purchase.builder()
                .customerEmail("janedoe@example.com")
                .customerName("Jane Doe")
                .shippingAddress("123 Shipping Address")
                .status(PurchaseStatus.PENDING)
                .total(new BigDecimal("99.99"))
                .build();

        entityManager.persist(purchase);

        Detail detail = Detail.builder()
                .purchase(purchase)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();

        entityManager.persist(detail);
        entityManager.flush();

        Long detailId = detail.getId();
        Long productId = product.getId();
        Long purchaseId = purchase.getId();

        entityManager.clear();

        Detail found = entityManager.find(Detail.class, detailId);

        assertNotNull(found);
        assertEquals(2, found.getQuantity());
        assertEquals(0, new BigDecimal("99.99").compareTo(found.getUnitPrice()));
        assertEquals(purchaseId, found.getPurchase().getId());
        assertEquals(productId, found.getProduct().getId());
    }

    @Test
    void testDetailShouldPersistPurchaseItemsThroughCascade() {
        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("30.00"))
                .stock(20)
                .build();

        entityManager.persist(product);

        Purchase purchase = Purchase.builder()
                .customerEmail("janedoe@example.com")
                .customerName("Jane Doe")
                .shippingAddress("123 Shipping Address")
                .total(new BigDecimal("60.00"))
                .build();

        Detail detail = Detail.builder()
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        purchase.addItem(detail);

        entityManager.persist(purchase);
        entityManager.flush();

        Long purchaseId = purchase.getId();

        entityManager.clear();

        Purchase found = entityManager.find(Purchase.class, purchaseId);

        assertNotNull(found);
        assertEquals(1, found.getItems().size());

        Detail foundDetail = found.getItems().getFirst();
        assertEquals(2, foundDetail.getQuantity());
        assertEquals(purchaseId, foundDetail.getPurchase().getId());
        assertEquals("Product", foundDetail.getProduct().getName());
    }

    @Test
    void testDetailShouldDeletePurchaseAndItsDetailsThroughCascade() {
        Product product = Product.builder()
                .name("Product")
                .price(new BigDecimal("30.00"))
                .stock(20)
                .build();

        entityManager.persist(product);

        Purchase purchase = Purchase.builder()
                .customerName("Jane Doe")
                .build();

        Detail detail = Detail.builder()
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        purchase.addItem(detail);

        entityManager.persist(purchase);
        entityManager.flush();

        Long purchaseId = purchase.getId();
        Long detailId = detail.getId();

        entityManager.remove(purchase);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Purchase.class, purchaseId));
        assertNull(entityManager.find(Detail.class, detailId));
    }
}
