package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNumberUnavailable;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.services.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final int ACCOUNT_NUMBER = 1234;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

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
                .andExpect(jsonPath("$.status").value(503));
    }
}
