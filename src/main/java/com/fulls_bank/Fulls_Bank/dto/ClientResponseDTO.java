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
public class ClientResponseDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "12345678901")
    private String cpf;
    @Schema(example = "Ana")
    private String name;
    @Schema(example = "Silva")
    private String lastName;
}
