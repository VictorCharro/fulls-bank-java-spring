package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNumberUnavailable;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidPeriod;
import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.services.AccountService;
import com.fulls_bank.Fulls_Bank.services.TransactionService;
import com.fulls_bank.Fulls_Bank.security.ApiSecurityTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@ApiSecurityTest
@WithMockUser(roles = "ADMIN")
class AccountControllerTest {

    private static final int ACCOUNT_NUMBER = 1234;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void searchAccountByNumber_withExistingNumber_shouldReturnAccountDto() throws Exception {
        Account account = new Account();
        account.setAccountNumber(ACCOUNT_NUMBER);
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setAccountNumber(ACCOUNT_NUMBER);
        dto.setType(AccountType.CHECKING);
        dto.setBalance(new BigDecimal("100"));
        when(accountService.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(account);
        when(accountService.toResponseDTO(account)).thenReturn(dto);

        mockMvc.perform(get("/account/number/{number}", ACCOUNT_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void searchAccountByNumber_withUnknownNumber_shouldReturn404() throws Exception {
        when(accountService.findAccountByAccountNumber(ACCOUNT_NUMBER))
                .thenThrow(new AccountNotFound("Account not found"));

        mockMvc.perform(get("/account/number/{number}", ACCOUNT_NUMBER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void searchAccountById_withUnknownId_shouldReturn404() throws Exception {
        when(accountService.findAccountById(99L)).thenThrow(new IdNotFound("ID not found"));

        mockMvc.perform(get("/account/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("ID not found"));
    }

    @Test
    void deleteAccount_withUnknownId_shouldReturn404() throws Exception {
        doThrow(new IdNotFound("ID not found")).when(accountService).deleteAccount(99L);

        mockMvc.perform(delete("/account/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ID not found"));
    }

    @Test
    void deleteAccount_withTransactions_shouldReturn409() throws Exception {
        doThrow(new ResourceInUse("Account has transactions and cannot be deleted"))
                .when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/account/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Account has transactions and cannot be deleted"));
    }

    @Test
    void createAccount_whenNoAccountNumberIsAvailable_shouldReturn503() throws Exception {
        when(accountService.createAccount(any()))
                .thenThrow(new AccountNumberUnavailable("Could not generate a unique account number, try again later"));

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CHECKING\",\"clientId\":1}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message")
                        .value("Could not generate a unique account number, try again later"));
    }

    @Test
    void statement_withPeriodAndPaging_shouldPassParametersAndReturnPage() throws Exception {
        Transaction transaction = new Transaction();
        TransactionResponseDTO dto = new TransactionResponseDTO(9L, new BigDecimal("10.00"),
                LocalDateTime.of(2026, 3, 10, 12, 0), TransactionType.DEPOSIT);
        Page<Transaction> page = new PageImpl<>(List.of(transaction), PageRequest.of(1, 5), 11);
        when(transactionService.getStatement(eq(7L), eq(LocalDate.of(2026, 3, 1)), eq(LocalDate.of(2026, 3, 31)), any()))
                .thenReturn(page);
        when(transactionService.toResponseDTO(transaction)).thenReturn(dto);

        mockMvc.perform(get("/account/{id}/statement", 7L)
                        .param("from", "2026-03-01").param("to", "2026-03-31")
                        .param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(9))
                .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.totalElements").value(11))
                .andExpect(jsonPath("$.page.totalPages").value(3));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionService).getStatement(eq(7L), eq(LocalDate.of(2026, 3, 1)),
                eq(LocalDate.of(2026, 3, 31)), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(5, pageable.getValue().getPageSize());
    }

    @Test
    void statement_withoutParameters_shouldUseDefaultPageAndNoPeriod() throws Exception {
        when(transactionService.getStatement(eq(7L), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/account/{id}/statement", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(20));
    }

    @Test
    void statement_withHugePageSize_shouldBeLimitedTo100() throws Exception {
        when(transactionService.getStatement(eq(7L), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mockMvc.perform(get("/account/{id}/statement", 7L).param("size", "100000"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionService).getStatement(eq(7L), eq(null), eq(null), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
    }

    @Test
    void statement_whenStartIsAfterEnd_shouldReturn400() throws Exception {
        when(transactionService.getStatement(eq(7L), any(), any(), any()))
                .thenThrow(new InvalidPeriod("The start date must not be after the end date"));

        mockMvc.perform(get("/account/{id}/statement", 7L).param("from", "2026-04-01").param("to", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The start date must not be after the end date"));
    }

    @Test
    void statement_withUnknownAccount_shouldReturn404() throws Exception {
        when(transactionService.getStatement(eq(99L), any(), any(), any()))
                .thenThrow(new IdNotFound("Account not found"));

        mockMvc.perform(get("/account/{id}/statement", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
