package com.fulls_bank.Fulls_Bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDTO {

    @Schema(description = "CPF com 11 dígitos (único)", example = "12345678901")
    @NotBlank(message = "The CPF cannot be blank")
    @Pattern(regexp = "\\d{11}", message = "CPF must have 11 digits")
    private String cpf;

    @Schema(example = "Ana")
    @NotBlank(message = "The name cannot be blank")
    private String name;

    @Schema(example = "Silva")
    @NotBlank(message = "The Last name cannot be blank")
    private String lastName;
}
