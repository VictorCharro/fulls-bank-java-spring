package com.fulls_bank.Fulls_Bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import com.fulls_bank.Fulls_Bank.validation.Cpf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDTO {

    @Schema(description = "CPF válido, com ou sem máscara (é gravado só com os dígitos); único", example = "529.982.247-25")
    @NotBlank(message = "The CPF cannot be blank")
    @Cpf
    private String cpf;

    @Schema(example = "Ana")
    @NotBlank(message = "The name cannot be blank")
    private String name;

    @Schema(example = "Silva")
    @NotBlank(message = "The Last name cannot be blank")
    private String lastName;
}
