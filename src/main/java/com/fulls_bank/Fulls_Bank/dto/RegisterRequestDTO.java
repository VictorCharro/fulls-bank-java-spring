package com.fulls_bank.Fulls_Bank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank
    @Email
    private String email;

    // O limite do BCrypt é de 72 bytes (não caracteres); a checagem em bytes está em isPasswordWithinBcryptLimit
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @JsonIgnore
    @AssertTrue(message = "password must have at most 72 bytes in UTF-8")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
