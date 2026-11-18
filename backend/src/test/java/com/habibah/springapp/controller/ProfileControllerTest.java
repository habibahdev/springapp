package com.habibah.springapp.controller;

import com.habibah.springapp.security.JwtAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;
import com.habibah.springapp.dto.profile.PasswordChangeRequest;
import com.habibah.springapp.dto.profile.ProfileRequest;
import com.habibah.springapp.dto.profile.ProfileResponse;
import com.habibah.springapp.exception.BusinessException;
import com.habibah.springapp.exception.GlobalExceptionHandler;
import com.habibah.springapp.exception.NotFoundException;
import com.habibah.springapp.service.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.habibah.springapp.security.SecurityConfig;
import com.habibah.springapp.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class ProfileControllerTest {

    private static final String BASE_URL = "/api/users/me";
    private static final String EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean(name = "customerUserDetailsService")
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void getProfileShouldReturnProfile() throws Exception {
        ProfileResponse response = new ProfileResponse(
                1L,
                "John Doe",
                EMAIL
        );

        when(profileService.getProfile(EMAIL)).thenReturn(response);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value(EMAIL));

        verify(profileService).getProfile(EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void updateProfileShouldReturnUpdatedProfile() throws Exception {
        ProfileRequest request = new ProfileRequest("Jane Doe");

        ProfileResponse response = new ProfileResponse(
                1L,
                "Jane Doe",
                EMAIL
        );

        when(profileService.updateProfile(request, EMAIL)).thenReturn(response);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value(EMAIL));

        verify(profileService).updateProfile(request, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void updateProfileShouldReturnBadRequestWhenFullNameIsBlank() throws Exception {
        String request = """
                {
                    "fullName": ""
                }
                """;

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Données invalides"))
                .andExpect(jsonPath("$.errors.fullName").exists());

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void getProfileShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(profileService.getProfile(EMAIL))
                .thenThrow(new NotFoundException("Utilisateur introuvable"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Utilisateur introuvable"));

        verify(profileService).getProfile(EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void changePasswordShouldReturnNoContent() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "OldPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(profileService).changePassword(request, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void changePasswordShouldReturnConflictWhenCurrentPasswordIsIncorrect() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "WrongPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        doThrow(new BusinessException("Le mot de passe actuel est incorrect."))
                .when(profileService).changePassword(request, EMAIL);

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Le mot de passe actuel est incorrect."));

        verify(profileService).changePassword(request, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void changePasswordShouldReturnConflictWhenConfirmationDoesNotMatch() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "OldPassword123!",
                "NewPassword123!",
                "DifferentPassword123!"
        );

        doThrow(new BusinessException("Les nouveaux mots de passe ne correspondent pas."))
                .when(profileService).changePassword(request, EMAIL);

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Les nouveaux mots de passe ne correspondent pas."));

        verify(profileService).changePassword(request, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void changePasswordShouldReturnBadRequestWhenNewPasswordIsTooShort() throws Exception {
        String request = """
                {
                    "currentPassword": "OldPassword123!",
                    "newPassword": "short",
                    "confirmation": "short"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Données invalides"))
                .andExpect(jsonPath("$.errors.newPassword").exists());

        verifyNoInteractions(profileService);
    }

    @Test
    void getProfileShouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getProfileShouldReturnForbiddenWhenUserIsAdmin() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(profileService);
    }
}