package com.fulls_bank.Fulls_Bank.dto;

import com.fulls_bank.Fulls_Bank.enums.AccountType;
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

    private int accountNumber;
    private AccountType type;
    private BigDecimal balance;
    private List<TransactionResponseDTO> sentTransactions;
    private List<TransactionResponseDTO> receivedTransactions;
}
