package com.habibah.springapp.controller;

import com.habibah.springapp.dto.DetailRequest;
import com.habibah.springapp.dto.DetailResponse;
import com.habibah.springapp.dto.PurchaseRequest;
import com.habibah.springapp.dto.PurchaseResponse;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import com.habibah.springapp.service.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PurchaseController purchaseController;

    private PurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        purchaseResponse = new PurchaseResponse(
                1L,
                "Jane Doe",
                "janedoe@example.com",
                "123 Shipping Address",
                PurchaseStatus.values()[0],
                LocalDateTime.now(),
                new BigDecimal("50.00"),
                List.of(new DetailResponse(
                        1L,
                        "Clavier",
                        2,
                        new BigDecimal("25.00")
                ))
        );
    }

    @Test
    void testShouldCreatePurchaseForAuthenticatedUser() {
        when(authentication.getName()).thenReturn("janedoe@example.com");

        PurchaseRequest request = new PurchaseRequest(
                "Jane Doe",
                "janedoe@example.com",
                "123 Shipping Address",
                List.of(new DetailRequest(1L, 2))
        );

        when(purchaseService.create(request, "janedoe@example.com"))
                .thenReturn(purchaseResponse);

        PurchaseResponse result = purchaseController.create(request, authentication);

        assertEquals(purchaseResponse, result);
        verify(purchaseService).create(request, "janedoe@example.com");
    }

    @Test
    void testShouldReturnAuthenticatedUsersPurchases() {
        when(authentication.getName()).thenReturn("janedoe@example.com");

        when(purchaseService.findByUser("janedoe@example.com"))
                .thenReturn(List.of(purchaseResponse));

        List<PurchaseResponse> result =
                purchaseController.myPurchases(authentication);

        assertEquals(1, result.size());
        assertEquals(purchaseResponse, result.getFirst());
        verify(purchaseService).findByUser("janedoe@example.com");
    }

    @Test
    void testShouldReturnAllPurchases() {
        when(purchaseService.findAll()).thenReturn(List.of(purchaseResponse));

        List<PurchaseResponse> result = purchaseController.purchases();

        assertEquals(1, result.size());
        assertEquals(purchaseResponse, result.getFirst());
        verify(purchaseService).findAll();
    }

    @Test
    void testShouldReturnPurchaseById() {
        when(purchaseService.findById(1L)).thenReturn(purchaseResponse);

        PurchaseResponse result = purchaseController.get(1L);

        assertEquals(purchaseResponse, result);
        verify(purchaseService).findById(1L);
    }

    @Test
    void testShouldDelegateChangeStatusToPurchaseService() {
        PurchaseStatus status = PurchaseStatus.values()[0];

        when(purchaseService.updateStatus(1L, status))
                .thenReturn(purchaseResponse);

        PurchaseResponse result = purchaseController.updateStatus(1L, status);

        assertEquals(purchaseResponse, result);
        verify(purchaseService).updateStatus(1L, status);
    }
}