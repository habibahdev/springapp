package com.habibah.springapp.service;

import com.habibah.springapp.dto.profile.PasswordChangeRequest;
import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.enums.Role;
import com.habibah.springapp.exception.BusinessException;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String FULL_NAME = "John Doe";
    private static final String PASSWORD = "Password123!";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .fullName(FULL_NAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();
    }

    @Test
    void shouldChangePassword() {
        String newPassword = "NewPassword123!";
        String newEncodedPassword = "newEncodedPassword";

        PasswordChangeRequest request = new PasswordChangeRequest(
                PASSWORD,
                newPassword,
                newPassword
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        when(passwordEncoder.matches(newPassword, ENCODED_PASSWORD))
                .thenReturn(false);

        when(passwordEncoder.encode(newPassword))
                .thenReturn(newEncodedPassword);

        profileService.changePassword(request, EMAIL);

        assertEquals(newEncodedPassword, user.getPassword());

        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "WrongPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.currentPassword(),
                ENCODED_PASSWORD
        )).thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> profileService.changePassword(request, EMAIL)
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordConfirmationDoesNotMatch() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                PASSWORD,
                "NewPassword123!",
                "DifferentPassword123!"
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> profileService.changePassword(request, EMAIL)
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsSameAsCurrent() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                PASSWORD,
                PASSWORD,
                PASSWORD
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> profileService.changePassword(request, EMAIL)
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                PASSWORD,
                "NewPassword123!",
                "NewPassword123!"
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> profileService.changePassword(request, EMAIL)
        );

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}