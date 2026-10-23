package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.LoginRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.RegisterRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.TokenResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.UserResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        AppUser user = authService.register(dto);
        UserResponseDTO response = new UserResponseDTO(user.getId(), user.getEmail(), user.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }
}
