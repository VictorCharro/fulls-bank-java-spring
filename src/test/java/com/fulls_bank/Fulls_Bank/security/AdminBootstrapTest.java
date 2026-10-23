package com.fulls_bank.Fulls_Bank.security;

import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.enums.Role;
import com.fulls_bank.Fulls_Bank.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void run_withEmailAndPassword_shouldCreateAdminWithHashedPassword() {
        when(appUserRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("admin-password")).thenReturn("hashed");

        new AdminBootstrap(appUserRepository, passwordEncoder, " Admin@Example.com ", "admin-password").run(null);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertEquals("admin@example.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPasswordHash());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void run_whenAdminAlreadyExists_shouldNotCreateAnother() {
        when(appUserRepository.existsByEmail("admin@example.com")).thenReturn(true);

        new AdminBootstrap(appUserRepository, passwordEncoder, "admin@example.com", "admin-password").run(null);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void run_withoutVariables_shouldDoNothing() {
        new AdminBootstrap(appUserRepository, passwordEncoder, "", "").run(null);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void run_withOnlyOneVariable_shouldDoNothing() {
        new AdminBootstrap(appUserRepository, passwordEncoder, "admin@example.com", "").run(null);

        verify(appUserRepository, never()).save(any());
    }
}
