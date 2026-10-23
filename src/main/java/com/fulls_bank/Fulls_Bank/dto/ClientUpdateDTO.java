package com.fulls_bank.Fulls_Bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Corpo do {@code PUT /client/{id}}: o CPF não muda, por isso nem faz parte do corpo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateDTO {

    @Schema(example = "Ana")
    @NotBlank(message = "The name cannot be blank")
    private String name;

    @Schema(example = "Silva")
    @NotBlank(message = "The Last name cannot be blank")
    private String lastName;
}
