package com.fulls_bank.Fulls_Bank.dto;

import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {

    @NotNull
    @Schema(description = "Conta de origem (saque e transferência)", example = "1")
    private Long senderId = 0L;

    @NotNull
    @Schema(description = "Conta de destino (depósito e transferência)", example = "2")
    private Long receiverId = 0L;

    @NotNull(message = "The amount cannot be null")
    @Positive(message = "The amount must be greater than zero")
    @Schema(description = "Valor, maior que zero", example = "50.00")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Tipo da transação", example = "TRANSFER")
    private TransactionType type;
}