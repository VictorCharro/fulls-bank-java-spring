package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Extrato com PostgreSQL real: filtro por período, origem/destino, ordenação e paginação. */
class StatementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private Account accountA;
    private Account accountB;
    private Account accountC;

    @BeforeEach
    void seed() {
        Client client = new Client();
        client.setCpf("52998224725");
        client.setName("Ana");
        client.setLastName("Silva");
        client = clientRepository.save(client);
        accountA = saveAccount(client, 1001);
        accountB = saveAccount(client, 1002);
        accountC = saveAccount(client, 1003);

        // Conta A: depósito (sem origem), saque (sem destino) e duas transferências, uma de cada sentido
        save(TransactionType.DEPOSIT, null, accountA, "100.00", LocalDateTime.of(2026, 3, 1, 0, 0, 0));
        save(TransactionType.WITHDRAWAL, accountA, null, "10.00", LocalDateTime.of(2026, 3, 10, 12, 0));
        save(TransactionType.TRANSFER, accountA, accountB, "20.00", LocalDateTime.of(2026, 3, 20, 8, 30));
        save(TransactionType.TRANSFER, accountB, accountA, "5.00", LocalDateTime.of(2026, 3, 31, 23, 59, 59));
        // Fora do extrato da conta A
        save(TransactionType.TRANSFER, accountB, accountC, "7.00", LocalDateTime.of(2026, 3, 15, 9, 0));
        save(TransactionType.DEPOSIT, null, accountC, "1.00", LocalDateTime.of(2026, 3, 16, 9, 0));
        // Fora do período de março, mas da conta A
        save(TransactionType.DEPOSIT, null, accountA, "3.00", LocalDateTime.of(2026, 2, 28, 23, 59, 59));
        save(TransactionType.DEPOSIT, null, accountA, "4.00", LocalDateTime.of(2026, 4, 1, 0, 0, 0));
    }

    @Test
    void statement_withoutFilter_shouldListOnlyTheAccountTransactionsNewestFirst() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(6))
                .andExpect(jsonPath("$.content[0].amount").value(4.0))
                .andExpect(jsonPath("$.content[1].amount").value(5.0))
                .andExpect(jsonPath("$.content[2].amount").value(20.0))
                .andExpect(jsonPath("$.content[3].amount").value(10.0))
                .andExpect(jsonPath("$.content[4].amount").value(100.0))
                .andExpect(jsonPath("$.content[5].amount").value(3.0));
    }

    @Test
    void statement_withPeriod_shouldIncludeBothBoundaryDaysInTheirEntirety() throws Exception {
        // 01/03 00:00:00 e 31/03 23:59:59 entram; 28/02 23:59:59 e 01/04 00:00:00 ficam de fora
        mockMvc.perform(get("/account/{id}/statement", accountA.getId())
                        .param("from", "2026-03-01").param("to", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].amount").value(5.0))
                .andExpect(jsonPath("$.content[3].amount").value(100.0));
    }

    @Test
    void statement_withOnlyStartOrOnlyEnd_shouldFilterOneSide() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("from", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("to", "2026-03-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void statement_withSingleDay_shouldReturnOnlyThatDay() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId())
                        .param("from", "2026-03-10").param("to", "2026-03-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("WITHDRAWAL"));
    }

    @Test
    void statement_shouldPaginateWithoutRepeatingOrSkipping() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("size", "4").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.page.totalElements").value(6))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.content[3].amount").value(10.0));
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("size", "4").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(100.0))
                .andExpect(jsonPath("$.content[1].amount").value(3.0));
    }

    @Test
    void statement_withTheSameInstant_shouldKeepAStableOrderById() throws Exception {
        LocalDateTime instant = LocalDateTime.of(2026, 5, 5, 10, 0);
        Long first = save(TransactionType.DEPOSIT, null, accountB, "1.00", instant).getId();
        Long second = save(TransactionType.DEPOSIT, null, accountB, "2.00", instant).getId();

        mockMvc.perform(get("/account/{id}/statement", accountB.getId()).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(second.intValue()));
        mockMvc.perform(get("/account/{id}/statement", accountB.getId()).param("size", "1").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(first.intValue()));
    }

    @Test
    void statement_withClientSortParameter_shouldBeIgnoredInsteadOfFailing() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("sort", "doesNotExist,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(4.0));
    }

    @Test
    void statement_beyondTheLastPage_shouldReturnAnEmptyPage() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("page", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(6));
    }

    @Test
    void statement_withHugePageSize_shouldBeLimitedTo100() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("size", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void statement_withInvalidInput_shouldReturn4xx() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId())
                        .param("from", "2026-04-01").param("to", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The start date must not be after the end date"));
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("from", "01/03/2026"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/account/{id}/statement", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void statement_withNegativePageOrNonPositiveSize_shouldReturn400() throws Exception {
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/account/{id}/statement", accountA.getId()).param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    private Account saveAccount(Client client, int number) {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setBalance(BigDecimal.ZERO);
        account.setType(AccountType.CHECKING);
        account.setClient(client);
        return accountRepository.save(account);
    }

    private Transaction save(TransactionType type, Account sender, Account receiver, String amount,
                             LocalDateTime timestamp) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setTimestamp(timestamp);
        return transactionRepository.save(transaction);
    }
}
