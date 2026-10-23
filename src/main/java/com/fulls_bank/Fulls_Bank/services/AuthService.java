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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Todo cadastro público nasce com o papel USER; administradores são criados fora da API. */
    public AppUser register(RegisterRequestDTO dto) {
        String email = normalize(dto.getEmail());
        if (appUserRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegistered("A user with this email already exists");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        try {
            return appUserRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Dois cadastros simultâneos com o mesmo e-mail: o índice único do banco decide
            throw new EmailAlreadyRegistered("A user with this email already exists");
        }
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        // Mesma resposta para e-mail desconhecido e senha errada, para não revelar quais e-mails existem
        AppUser user = appUserRepository.findByEmail(normalize(dto.getEmail()))
                .filter(found -> passwordEncoder.matches(dto.getPassword(), found.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentials("Invalid email or password"));
        return jwtService.generateToken(user);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
