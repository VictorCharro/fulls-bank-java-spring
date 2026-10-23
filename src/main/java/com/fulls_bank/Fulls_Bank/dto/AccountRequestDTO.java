package com.fulls_bank.Fulls_Bank.dto;

import com.fulls_bank.Fulls_Bank.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDTO {

    @NotNull
    @Schema(description = "Tipo da conta", example = "CHECKING")
    private AccountType type;

    @NotNull
    @Schema(description = "Id de um cliente existente", example = "1")
    private Long clientId;
}
