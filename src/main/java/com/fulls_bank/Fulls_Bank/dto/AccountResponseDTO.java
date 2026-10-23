package com.fulls_bank.Fulls_Bank.dto;

import com.fulls_bank.Fulls_Bank.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDTO {

    @Schema(description = "Número da conta (6 dígitos, gerado automaticamente)", example = "482913")
    private int accountNumber;
    private AccountType type;
    @Schema(example = "150.00")
    private BigDecimal balance;
    private List<TransactionResponseDTO> sentTransactions;
    private List<TransactionResponseDTO> receivedTransactions;
}
