package com.fulls_bank.Fulls_Bank.dto;

import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {

    public TransactionResponseDTO(Long id, BigDecimal amount, LocalDateTime timestamp, TransactionType type) {
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
    }

    @Schema(example = "12")
    private Long id;
    @Schema(example = "50.00")
    private BigDecimal amount;
    @Schema(example = "2026-03-20T08:30:00")
    private LocalDateTime timestamp;
    private TransactionType type;

    private Long senderId;
    private Long receiverId;
}