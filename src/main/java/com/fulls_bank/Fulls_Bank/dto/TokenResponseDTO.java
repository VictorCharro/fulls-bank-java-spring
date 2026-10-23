package com.fulls_bank.Fulls_Bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
