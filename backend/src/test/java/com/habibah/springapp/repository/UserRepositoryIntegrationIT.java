package com.habibah.springapp.repository;

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
public class UserRepositoryIntegrationIT {
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
    private UserRepository userRepository;

    @Test
    void testShouldSaveAndRetrieveUserByEmail() {
        User user = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.saveAndFlush(user);

        var result = userRepository.findByEmail("janedoe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("janedoe@example.com");
        assertThat(result.get().getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void testShouldReturnEmptyWhenEmailDoesNotExist() {
        var result = userRepository.findByEmail("unknown@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    void testShouldReturnTrueWhenEmailExists() {
        User user = User.builder()
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.saveAndFlush(user);

        assertThat(userRepository.existsByEmail("janedoe@example.com")).isTrue();
    }

    @Test
    void testShouldReturnFalseWhenEmailDoesNotExist() {
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }
}
