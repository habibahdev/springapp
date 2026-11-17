package com.habibah.springapp.controller;

import com.habibah.springapp.dto.address.AddressRequest;
import com.habibah.springapp.dto.address.AddressResponse;
import com.habibah.springapp.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    private static final String USER_EMAIL = "user@example.com";

    private static final Long ADDRESS_ID = 1L;

    private MockMvc mockMvc;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController)
                .build();
    }

    @Test
    void testShouldGetUserAddresses() throws Exception {
        AddressResponse response = createAddressResponse(false);

        when(addressService.getUserAddresses(USER_EMAIL))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/addresses")
                        .principal(createPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ADDRESS_ID))
                .andExpect(jsonPath("$[0].label").value("Maison"))
                .andExpect(jsonPath("$[0].city").value("Rouen"))
                .andExpect(jsonPath("$[0].defaultAddress").value(false));

        verify(addressService).getUserAddresses(USER_EMAIL);
    }

    @Test
    void testShouldCreateAddress() throws Exception {
        AddressResponse response = createAddressResponse(false);

        when(addressService.createAddress(
                org.mockito.ArgumentMatchers.any(AddressRequest.class),
                eq(USER_EMAIL)
        )).thenReturn(response);

        mockMvc.perform(post("/api/addresses")
                        .principal(createPrincipal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAddressJson(false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ADDRESS_ID))
                .andExpect(jsonPath("$.label").value("Maison"))
                .andExpect(jsonPath("$.defaultAddress").value(false));

        verify(addressService).createAddress(
                org.mockito.ArgumentMatchers.any(AddressRequest.class),
                eq(USER_EMAIL)
        );
    }

    @Test
    void testShouldUpdateAddress() throws Exception {
        AddressResponse response = createAddressResponse(false);

        when(addressService.updateAddress(
                eq(ADDRESS_ID),
                org.mockito.ArgumentMatchers.any(AddressRequest.class),
                eq(USER_EMAIL)
        )).thenReturn(response);

        mockMvc.perform(put("/api/addresses/{id}", ADDRESS_ID)
                        .principal(createPrincipal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAddressJson(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADDRESS_ID))
                .andExpect(jsonPath("$.city").value("Rouen"));

        verify(addressService).updateAddress(
                eq(ADDRESS_ID),
                org.mockito.ArgumentMatchers.any(AddressRequest.class),
                eq(USER_EMAIL)
        );
    }

    @Test
    void testShouldDeleteAddress() throws Exception {
        mockMvc.perform(delete("/api/addresses/{id}", ADDRESS_ID)
                        .principal(createPrincipal()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(addressService).deleteAddress(ADDRESS_ID, USER_EMAIL);
    }

    @Test
    void testShouldSetDefaultAddress() throws Exception {
        AddressResponse response = createAddressResponse(true);

        when(addressService.setDefaultAddress(ADDRESS_ID, USER_EMAIL))
                .thenReturn(response);

        mockMvc.perform(patch(
                        "/api/addresses/{id}/default",
                        ADDRESS_ID
                ).principal(createPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADDRESS_ID))
                .andExpect(jsonPath("$.defaultAddress").value(true));

        verify(addressService).setDefaultAddress(
                ADDRESS_ID,
                USER_EMAIL
        );
    }

    /**
     * Crée un principal représentant l'utilisateur authentifié.
     *
     * @return le principal de test
     */
    private Principal createPrincipal() {
        return () -> USER_EMAIL;
    }

    /**
     * Construit une réponse d'adresse pour les tests.
     *
     * @param defaultAddress indique si l'adresse est définie par défaut
     * @return une réponse d'adresse
     */
    private AddressResponse createAddressResponse(boolean defaultAddress) {
        return new AddressResponse(
                ADDRESS_ID,
                "Maison",
                "Utilisateur Test",
                "10 rue de la Paix",
                null,
                "76000",
                "Rouen",
                "France",
                "0600000000",
                defaultAddress
        );
    }

    /**
     * Construit le JSON utilisé pour créer ou modifier une adresse.
     *
     * @param defaultAddress indique si l'adresse est définie par défaut
     * @return le contenu JSON
     */
    private String createAddressJson(boolean defaultAddress) {
        return """
                {
                    "label": "Maison",
                    "recipientName": "Utilisateur Test",
                    "street": "10 rue de la Paix",
                    "additionalAddress": null,
                    "postalCode": "76000",
                    "city": "Rouen",
                    "country": "France",
                    "phone": "0600000000",
                    "defaultAddress": %s
                }
                """.formatted(defaultAddress);
    }
}
