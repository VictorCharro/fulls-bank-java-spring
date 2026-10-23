package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptimisticLockIntegrationTest extends AbstractIntegrationTest {

    @Test
    void save_withStaleVersion_shouldThrowAndKeepTheFirstUpdate() {
        Long accountId = saveAccount().getId();
        Account firstReader = accountRepository.findById(accountId).orElseThrow();
        Account secondReader = accountRepository.findById(accountId).orElseThrow();

        firstReader.setBalance(new BigDecimal("100.00"));
        accountRepository.save(firstReader);

        secondReader.setBalance(new BigDecimal("50.00"));
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> accountRepository.save(secondReader));

        Account stored = accountRepository.findById(accountId).orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(stored.getBalance()));
        assertEquals(1L, stored.getVersion());
    }

    private Account saveAccount() {
        Client client = new Client();
        client.setCpf("12345678901");
        client.setName("Ana");
        client.setLastName("Silva");
        client = clientRepository.save(client);

        Account account = new Account();
        account.setAccountNumber(1001);
        account.setBalance(BigDecimal.ZERO);
        account.setType(AccountType.CHECKING);
        account.setClient(client);
        return accountRepository.save(account);
    }
}
