package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.LoginRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.RegisterRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.TokenResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.enums.Role;
import com.fulls_bank.Fulls_Bank.exceptions.EmailAlreadyRegistered;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidCredentials;
import com.fulls_bank.Fulls_Bank.repositories.AppUserRepository;
import com.fulls_bank.Fulls_Bank.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_withNewEmail_shouldSaveUserRoleWithHashedPassword() {
        when(appUserRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret-123")).thenReturn("hashed");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = authService.register(new RegisterRequestDTO("  Ana@Example.com ", "secret-123"));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertEquals("ana@example.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPasswordHash());
        assertEquals(Role.USER, captor.getValue().getRole());
        assertSame(captor.getValue(), saved);
    }

    @Test
    void register_withExistingEmail_shouldThrowAndNotSave() {
        when(appUserRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegistered.class,
                () -> authService.register(new RegisterRequestDTO("ana@example.com", "secret-123")));

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void register_whenDatabaseRejectsDuplicateEmail_shouldThrowEmailAlreadyRegistered() {
        when(appUserRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret-123")).thenReturn("hashed");
        when(appUserRepository.save(any(AppUser.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(EmailAlreadyRegistered.class,
                () -> authService.register(new RegisterRequestDTO("ana@example.com", "secret-123")));
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        AppUser user = user("ana@example.com", "hashed", Role.USER);
        TokenResponseDTO token = new TokenResponseDTO("jwt", "Bearer", 3600);
        when(appUserRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret-123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(token);

        assertSame(token, authService.login(new LoginRequestDTO("Ana@example.com", "secret-123")));
    }

    @Test
    void login_withWrongPassword_shouldThrowInvalidCredentials() {
        AppUser user = user("ana@example.com", "hashed", Role.USER);
        when(appUserRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentials.class,
                () -> authService.login(new LoginRequestDTO("ana@example.com", "wrong")));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_withUnknownEmail_shouldThrowTheSameInvalidCredentials() {
        when(appUserRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        InvalidCredentials error = assertThrows(InvalidCredentials.class,
                () -> authService.login(new LoginRequestDTO("nobody@example.com", "secret-123")));

        assertEquals("Invalid email or password", error.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    private AppUser user(String email, String hash, Role role) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setRole(role);
        return user;
    }
}
