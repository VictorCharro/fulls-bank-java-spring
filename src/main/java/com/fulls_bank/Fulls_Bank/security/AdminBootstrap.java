package com.fulls_bank.Fulls_Bank.security;

import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.enums.Role;
import com.fulls_bank.Fulls_Bank.repositories.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Cria o primeiro administrador a partir das variáveis ADMIN_EMAIL e ADMIN_PASSWORD, se ambas estiverem
 * definidas e esse e-mail ainda não existir. Sem as variáveis, não faz nada; nenhuma senha fica no código.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
                          @Value("${app.admin.email:}") String adminEmail,
                          @Value("${app.admin.password:}") String adminPassword) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmail(email)) {
            return;
        }
        // Limite do BCrypt; falha na inicialização com uma mensagem clara em vez de um erro interno do encoder
        if (adminPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("ADMIN_PASSWORD must have at most 72 bytes in UTF-8");
        }
        AppUser admin = new AppUser();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        appUserRepository.save(admin);
        log.info("Administrator {} created from ADMIN_EMAIL/ADMIN_PASSWORD", email);
    }
}
