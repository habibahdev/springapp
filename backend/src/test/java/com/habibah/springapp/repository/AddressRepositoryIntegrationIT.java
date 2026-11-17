package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Address;
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
public class AddressRepositoryIntegrationIT {

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
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testShouldFindAllAddressesByUserEmailOrderedByDefaultAddressAndId() {
        User user = createUser("janedoe@example.com");

        Address firstAddress = createAddress(user, "Domicile", false);
        Address defaultAddress = createAddress(user, "Travail", true);
        Address lastAddress = createAddress(user, "Famille", false);

        addressRepository.save(firstAddress);
        addressRepository.save(defaultAddress);
        addressRepository.saveAndFlush(lastAddress);

        List<Address> addresses = addressRepository
                .findAllByUserEmailOrderByDefaultAddressDescIdDesc(user.getEmail());

        assertThat(addresses).hasSize(3);
        assertThat(addresses)
                .extracting(Address::getLabel)
                .containsExactly("Travail", "Famille", "Domicile");
    }

    @Test
    void testShouldReturnEmptyListWhenUserHasNoAddresses() {
        User user = createUser("janedoe@example.com");

        List<Address> addresses = addressRepository
                .findAllByUserEmailOrderByDefaultAddressDescIdDesc(user.getEmail());

        assertThat(addresses).isEmpty();
    }

    @Test
    void testShouldFindAddressByIdAndUserEmail() {
        User user = createUser("janedoe@example.com");
        Address address = addressRepository.saveAndFlush(
                createAddress(user, "Domicile", false)
        );

        var result = addressRepository.findByIdAndUserEmail(
                address.getId(),
                user.getEmail()
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getLabel()).isEqualTo("Domicile");
    }

    @Test
    void testShouldNotFindAddressWhenUserIsNotOwner() {
        User owner = createUser("owner@example.com");
        User anotherUser = createUser("another@example.com");

        Address address = addressRepository.saveAndFlush(
                createAddress(owner, "Domicile", false)
        );

        var result = addressRepository.findByIdAndUserEmail(
                address.getId(),
                anotherUser.getEmail()
        );

        assertThat(result).isEmpty();
    }

    @Test
    void testShouldFindDefaultAddressByUserEmail() {
        User user = createUser("janedoe@example.com");

        addressRepository.save(createAddress(user, "Domicile", false));
        addressRepository.saveAndFlush(createAddress(user, "Travail", true));

        var result = addressRepository.findByUserEmailAndDefaultAddressTrue(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getLabel()).isEqualTo("Travail");
        assertThat(result.orElseThrow().isDefaultAddress()).isTrue();
    }

    @Test
    void testShouldReturnEmptyWhenUserHasNoDefaultAddress() {
        User user = createUser("janedoe@example.com");

        addressRepository.saveAndFlush(createAddress(user, "Domicile", false));

        var result = addressRepository.findByUserEmailAndDefaultAddressTrue(user.getEmail());

        assertThat(result).isEmpty();
    }

    @Test
    void testShouldReturnTrueWhenUserHasDefaultAddress() {
        User user = createUser("janedoe@example.com");

        addressRepository.saveAndFlush(createAddress(user, "Domicile", true));

        boolean exists = addressRepository.existsByUserEmailAndDefaultAddressTrue(user.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void testShouldReturnFalseWhenUserHasNoDefaultAddress() {
        User user = createUser("janedoe@example.com");

        addressRepository.saveAndFlush(createAddress(user, "Domicile", false));

        boolean exists = addressRepository.existsByUserEmailAndDefaultAddressTrue(user.getEmail());

        assertThat(exists).isFalse();
    }

    private User createUser(final String email) {
        User user = User.builder()
                .email(email)
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        return userRepository.saveAndFlush(user);
    }

    private Address createAddress(
            final User user,
            final String label,
            final boolean defaultAddress
    ) {
        return Address.builder()
                .label(label)
                .recipientName("Jane Doe")
                .street("123 Main Street")
                .additionalAddress(null)
                .postalCode("76000")
                .city("Rouen")
                .country("France")
                .phone("0612345678")
                .defaultAddress(defaultAddress)
                .user(user)
                .build();
    }
}
