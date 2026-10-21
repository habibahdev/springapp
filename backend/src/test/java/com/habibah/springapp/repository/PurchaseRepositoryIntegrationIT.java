package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Purchase;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.enums.Role;
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
import java.util.List;

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
public class PurchaseRepositoryIntegrationIT {
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
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testShouldFindPurchaseByUserEmailOrderedByCreatedAtDescending() {
        User user = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.saveAndFlush(user);

        LocalDateTime oldDate = LocalDateTime.of(2016, 1, 10, 10, 0);
        LocalDateTime newDate = LocalDateTime.of(2026, 1, 12, 15, 0);

        Purchase oldPurchase = Purchase.builder()
                .customerName("Jane Doe")
                .customerEmail("janedoe@example.com")
                .shippingAddress("123 Shipping Address")
                .createdAt(oldDate)
                .total(new BigDecimal("50.00"))
                .user(user)
                .build();

        Purchase newPurchase = Purchase.builder()
                .customerName("Jane Doe")
                .customerEmail("janedoe@example.com")
                .shippingAddress("123 Shipping Address")
                .status(null)
                .createdAt(newDate)
                .total(new BigDecimal("90.00"))
                .user(user)
                .build();

        purchaseRepository.save(oldPurchase);
        purchaseRepository.saveAndFlush(newPurchase);

        List<Purchase> purchases = purchaseRepository.findByUserEmailOrderByCreatedAtDesc("janedoe@example.com");
        assertThat(purchases).hasSize(2);
        assertThat(purchases)
                .extracting(Purchase::getCreatedAt)
                .containsExactly(newDate, oldDate);
        assertThat(purchases)
                .extracting(Purchase::getTotal)
                .containsExactly(new BigDecimal("90.00"), new BigDecimal("50.00"));
    }

    @Test
    void testShouldReturnEmptyListWhenUserHasNPurchases() {
        User user = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.saveAndFlush(user);

        List<Purchase> purchases = purchaseRepository.findByUserEmailOrderByCreatedAtDesc("janedoe@example.com");
        assertThat(purchases).isEmpty();
    }
}
