package com.fulls_bank.Fulls_Bank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
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

    // 255 = tamanho da coluna app_user.email; acima disso o banco rejeitaria o cadastro
    @Schema(description = "E-mail do usuário (único; guardado em minúsculas)", example = "ana@example.com")
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    // O limite do BCrypt é de 72 bytes (não caracteres); a checagem em bytes está em isPasswordWithinBcryptLimit
    @Schema(description = "Senha: mínimo de 8 caracteres e máximo de 72 bytes em UTF-8", example = "senha-forte-123")
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @JsonIgnore
    @AssertTrue(message = "password must have at most 72 bytes in UTF-8")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
