package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.TokenResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.enums.Role;
import com.fulls_bank.Fulls_Bank.exceptions.EmailAlreadyRegistered;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidCredentials;
import com.fulls_bank.Fulls_Bank.security.ApiSecurityTest;
import com.fulls_bank.Fulls_Bank.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ApiSecurityTest
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_withValidBody_shouldReturn201WithoutExposingThePassword() throws Exception {
        AppUser saved = new AppUser(7L, "ana@example.com", "hashed", Role.USER);
        when(authService.register(any())).thenReturn(saved);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_withInvalidEmailAndShortPassword_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(authService, never()).register(any());
    }

    @Test
    void register_withMultibytePasswordOver72Bytes_shouldReturn400() throws Exception {
        // 40 caracteres, mas 80 bytes em UTF-8: passaria em @Size e quebraria o BCrypt
        String password = "é".repeat(40);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("72 bytes")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_withMultibytePasswordWithin72Bytes_shouldReturn201() throws Exception {
        // 36 caracteres, 72 bytes: no limite
        String password = "é".repeat(36);
        when(authService.register(any())).thenReturn(new AppUser(7L, "ana@example.com", "hashed", Role.USER));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void register_withExistingEmail_shouldReturn400() throws Exception {
        when(authService.register(any())).thenThrow(new EmailAlreadyRegistered("A user with this email already exists"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A user with this email already exists"));
    }

    @Test
    void login_withValidCredentials_shouldReturnBearerToken() throws Exception {
        when(authService.login(any())).thenReturn(new TokenResponseDTO("the-jwt", "Bearer", 3600));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("the-jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentials("Invalid email or password"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_withBlankFields_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
