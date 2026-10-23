package com.fulls_bank.Fulls_Bank.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "CPF com 11 dígitos (único)", example = "12345678901")
    @Size(min = 11, max = 11, message = "CPF must have 11 digits")
    @Column(unique = true)
    private String cpf;

    @Schema(example = "Ana")
    @NotBlank(message = "The name cannot be blank")
    private String name;

    @Schema(example = "Silva")
    @NotBlank(message = "The Last name cannot be blank")
    private String lastName;
}
