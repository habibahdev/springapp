package com.habibah.springapp.service;

import com.habibah.springapp.dto.address.AddressRequest;
import com.habibah.springapp.dto.address.AddressResponse;
import com.habibah.springapp.entity.Address;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.AddressRepository;
import com.habibah.springapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service de gestion des adresses.
 * <p>Les repositories sont simulés avec Mockito afin de tester les
 * règles métiers indépendamment de la base de données.</p>
 */
@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {
    private static final String USER_EMAIL = "user@test.com";

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private Address address;
    private AddressRequest addressRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email(USER_EMAIL)
                .fullName("Test User")
                .password("password")
                .build();

        address = Address.builder()
                .id(1L)
                .label("Domicile")
                .recipientName("Test User")
                .street("10 rue de la République")
                .postalCode("76000")
                .city("Rouen")
                .country("France")
                .defaultAddress(true)
                .user(user)
                .build();

        addressRequest = new AddressRequest(
                "Domicile",
                "Test User",
                "10 rue de la République",
                null,
                "76000",
                "Rouen",
                "France",
                "0600000000",
                false
        );
    }

    @Test
    void testShouldReturnUserAddresses() {
        when(addressRepository
                .findAllByUserEmailOrderByDefaultAddressDescIdDesc(USER_EMAIL))
                .thenReturn(List.of(address));

        List<AddressResponse> result =
                addressService.getUserAddresses(USER_EMAIL);

        assertEquals(1, result.size());
        assertEquals("Domicile", result.getFirst().label());
        assertTrue(result.getFirst().defaultAddress());

        verify(addressRepository)
                .findAllByUserEmailOrderByDefaultAddressDescIdDesc(USER_EMAIL);
    }

    @Test
    void testShouldCreateFirstAddressWithoutDefault() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse response = addressService.createAddress(
                addressRequest(false),
                USER_EMAIL
        );

        assertFalse(response.defaultAddress());

        verify(addressRepository, never())
                .findByUserEmailAndDefaultAddressTrue(USER_EMAIL);
    }

    @Test
    void testShouldAllowMultipleAddressesWithoutDefault() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse firstAddress = addressService.createAddress(
                addressRequest(false),
                USER_EMAIL
        );

        AddressResponse secondAddress = addressService.createAddress(
                addressRequest(false),
                USER_EMAIL
        );

        assertFalse(firstAddress.defaultAddress());
        assertFalse(secondAddress.defaultAddress());

        verify(addressRepository, times(2))
                .save(any(Address.class));
    }

    @Test
    void testShouldDeleteDefaultAddressWithoutAssigningAnotherDefault() {
        when(addressRepository.findByIdAndUserEmail(1L, USER_EMAIL))
                .thenReturn(Optional.of(address));

        addressService.deleteAddress(1L, USER_EMAIL);

        verify(addressRepository).delete(address);
        verify(addressRepository).flush();

        verify(addressRepository, never())
                .findAllByUserEmailOrderByDefaultAddressDescIdDesc(USER_EMAIL);

        verify(addressRepository, never())
                .save(any(Address.class));
    }

    @Test
    void testShouldRemoveDefaultStatusWhenUpdatingAddress() {
        address.setDefaultAddress(true);

        AddressRequest request = addressRequest(false);

        when(addressRepository.findByIdAndUserEmail(1L, USER_EMAIL))
                .thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse response = addressService.updateAddress(
                1L,
                request,
                USER_EMAIL
        );

        assertFalse(response.defaultAddress());
    }

    @Test
    void testShouldCreateAddressWithoutChangingExistingDefault() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse result =
                addressService.createAddress(addressRequest, USER_EMAIL);

        assertFalse(result.defaultAddress());

        verify(addressRepository, never())
                .findByUserEmailAndDefaultAddressTrue(USER_EMAIL);
    }

    @Test
    void testShouldChangeDefaultAddress() {
        Address newAddress = Address.builder()
                .id(2L)
                .label("Travail")
                .defaultAddress(false)
                .user(user)
                .build();

        when(addressRepository.findByIdAndUserEmail(2L, USER_EMAIL))
                .thenReturn(Optional.of(newAddress));

        when(addressRepository
                .findByUserEmailAndDefaultAddressTrue(USER_EMAIL))
                .thenReturn(Optional.of(address));

        AddressResponse result =
                addressService.setDefaultAddress(2L, USER_EMAIL);

        assertFalse(address.isDefaultAddress());
        assertTrue(newAddress.isDefaultAddress());
        assertEquals(2L, result.id());
        assertTrue(result.defaultAddress());

        verify(addressRepository).saveAndFlush(address);
        verify(addressRepository).saveAndFlush(newAddress);
    }

    @Test
    void testShouldUpdateAddress() {
        when(addressRepository.findByIdAndUserEmail(1L, USER_EMAIL))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse result =
                addressService.updateAddress(1L, addressRequest, USER_EMAIL);

        assertEquals("Domicile", result.label());
        assertEquals("Rouen", result.city());
        assertEquals("76000", result.postalCode());

        verify(addressRepository).save(address);
    }

    @Test
    void testShouldDeleteAddress() {
        when(addressRepository.findByIdAndUserEmail(1L, USER_EMAIL))
                .thenReturn(Optional.of(address));

        addressService.deleteAddress(1L, USER_EMAIL);

        verify(addressRepository).delete(address);
        verify(addressRepository).flush();
    }

    @Test
    void testShouldThrowExceptionWhenAddressDoesNotBelongToUser() {
        when(addressRepository.findByIdAndUserEmail(2L, USER_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> addressService.setDefaultAddress(2L, USER_EMAIL)
        );

        verify(addressRepository, never()).save(any(Address.class));
    }
    /**
     * Construit une requête de création ou de modification d'adresse.
     *
     * @param defaultAddress indique si l'adresse doit être définie par défaut
     * @return requête contenant les informations de l'adresse
     */
    private AddressRequest addressRequest(boolean defaultAddress) {
        return new AddressRequest(
                "Domicile",
                "Test Client",
                "10 rue de Paris",
                null,
                "76000",
                "Rouen",
                "France",
                "0612345678",
                defaultAddress
        );
    }

}
