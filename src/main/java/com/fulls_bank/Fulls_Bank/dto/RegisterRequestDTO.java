package com.fulls_bank.Fulls_Bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank
    @Email
    private String email;

    // BCrypt só considera os primeiros 72 bytes
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
