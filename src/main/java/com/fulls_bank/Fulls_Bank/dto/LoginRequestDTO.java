package com.fulls_bank.Fulls_Bank.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @Schema(example = "ana@example.com")
    @NotBlank
    private String email;

    @Schema(example = "senha-forte-123")
    @NotBlank
    private String password;
}
