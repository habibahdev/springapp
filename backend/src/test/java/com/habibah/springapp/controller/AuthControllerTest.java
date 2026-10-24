package com.habibah.springapp.controller;

import com.habibah.springapp.dto.AuthResponse;
import com.habibah.springapp.dto.LoginRequest;
import com.habibah.springapp.dto.RegisterRequest;
import com.habibah.springapp.entity.enums.Role;
import com.habibah.springapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testShouldDelegateRegisterToAuthService() {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "jane@example.com",
                "Password123"
        );

        AuthResponse expected = new AuthResponse(
                "jwt-token",
                "jane@example.com",
                "Jane Doe",
                Role.USER
        );

        when(authService.register(request)).thenReturn(expected);

        AuthResponse result = authController.register(request);

        assertEquals(expected, result);
        verify(authService).register(request);
    }

    @Test
    void testShouldDelegateLoginToAuthService() {
        LoginRequest request = new LoginRequest(
                "jane@example.com",
                "Password123"
        );

        AuthResponse expected = new AuthResponse(
                "jwt-token",
                "jane@example.com",
                "Jane Doe",
                Role.USER
        );

        when(authService.login(request)).thenReturn(expected);

        AuthResponse result = authController.login(request);

        assertEquals(expected, result);
        verify(authService).login(request);
    }
}
