package com.fulls_bank.Fulls_Bank.mappers;

import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionMapperTest {

    private static final LocalDateTime WHEN = LocalDateTime.of(2026, 3, 20, 8, 30);

    private final TransactionMapper mapper = new TransactionMapperImpl();

    @Test
    void toResponse_transfer_shouldCopyFieldsAndBothAccountIds() {
        Transaction transaction = transaction(TransactionType.TRANSFER, account(1L), account(2L));

        TransactionResponseDTO dto = mapper.toResponse(transaction);

        assertEquals(12L, dto.getId());
        assertEquals(new BigDecimal("50.00"), dto.getAmount());
        assertEquals(WHEN, dto.getTimestamp());
        assertEquals(TransactionType.TRANSFER, dto.getType());
        assertEquals(1L, dto.getSenderId());
        assertEquals(2L, dto.getReceiverId());
    }

    @Test
    void toResponse_deposit_shouldHaveNoSender() {
        TransactionResponseDTO dto = mapper.toResponse(transaction(TransactionType.DEPOSIT, null, account(2L)));

        assertNull(dto.getSenderId());
        assertEquals(2L, dto.getReceiverId());
    }

    @Test
    void toResponse_withdrawal_shouldHaveNoReceiver() {
        TransactionResponseDTO dto = mapper.toResponse(transaction(TransactionType.WITHDRAWAL, account(1L), null));

        assertEquals(1L, dto.getSenderId());
        assertNull(dto.getReceiverId());
    }

    @Test
    void toResponse_withNull_shouldReturnNull() {
        assertNull(mapper.toResponse(null));
    }

    private Transaction transaction(TransactionType type, Account sender, Account receiver) {
        Transaction transaction = new Transaction();
        transaction.setId(12L);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setTimestamp(WHEN);
        transaction.setType(type);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        return transaction;
    }

    private Account account(Long id) {
        Account account = new Account();
        account.setId(id);
        return account;
    }
}
