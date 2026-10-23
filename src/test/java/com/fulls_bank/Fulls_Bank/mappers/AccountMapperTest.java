package com.fulls_bank.Fulls_Bank.mappers;

import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapperImpl();

    @Test
    void toResponse_shouldCopyNumberTypeAndBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber(482913);
        account.setType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("150.25"));
        account.setClient(new Client());

        AccountResponseDTO dto = mapper.toResponse(account);

        assertEquals(482913, dto.getAccountNumber());
        assertEquals(AccountType.SAVINGS, dto.getType());
        assertEquals(new BigDecimal("150.25"), dto.getBalance());
    }

    @Test
    void toResponse_shouldNotExposeOrTouchTheTransactionLists() {
        Account account = new Account();
        account.setBalance(BigDecimal.TEN);
        // Uma coleção lazy não inicializada lançaria exceção se fosse lida; o mapeador não pode tocá-la
        account.setSentTransactions(new java.util.AbstractList<Transaction>() {
            @Override
            public Transaction get(int index) {
                throw new AssertionError("a lista de transações enviadas foi lida");
            }

            @Override
            public int size() {
                throw new AssertionError("a lista de transações enviadas foi lida");
            }
        });
        account.setReceivedTransactions(List.of());

        AccountResponseDTO dto = mapper.toResponse(account);

        assertNull(dto.getSentTransactions());
        assertNull(dto.getReceivedTransactions());
    }

    @Test
    void toResponse_withNull_shouldReturnNull() {
        assertNull(mapper.toResponse(null));
    }
}
