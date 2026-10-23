package com.fulls_bank.Fulls_Bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {

    @Schema(description = "JWT assinado (HS256)")
    private String accessToken;
    @Schema(example = "Bearer")
    private String tokenType;
    @Schema(description = "Validade em segundos", example = "3600")
    private long expiresIn;
}
