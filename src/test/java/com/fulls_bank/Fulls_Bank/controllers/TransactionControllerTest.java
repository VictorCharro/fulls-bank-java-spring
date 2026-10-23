package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidAmount;
import com.fulls_bank.Fulls_Bank.exceptions.SameAccountTransfer;
import com.fulls_bank.Fulls_Bank.exceptions.TransactionNotFound;
import com.fulls_bank.Fulls_Bank.services.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import com.fulls_bank.Fulls_Bank.entities.Account;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "null"})
    void createTransaction_withInvalidAmount_shouldReturn400AndNotCallService(String amount) throws Exception {
        String body = "{\"type\":\"DEPOSIT\",\"receiverId\":1,\"amount\":" + amount + "}";

        mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("amount")));

        verify(transactionService, never()).createTransaction(any());
    }

    @Test
    void createTransaction_withPositiveAmount_shouldReturn201() throws Exception {
        Transaction transaction = new Transaction();
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setType(TransactionType.DEPOSIT);
        response.setAmount(new BigDecimal("10.50"));
        when(transactionService.createTransaction(any())).thenReturn(transaction);
        when(transactionService.toResponseDTO(transaction)).thenReturn(response);

        String body = "{\"type\":\"DEPOSIT\",\"receiverId\":1,\"amount\":10.50}";

        mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(10.50));
    }

    @Test
    void getTransactionById_withExistingId_shouldReturnDto() throws Exception {
        Transaction transaction = new Transaction();
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setId(10L);
        response.setType(TransactionType.DEPOSIT);
        response.setAmount(new BigDecimal("10.50"));
        when(transactionService.findTransactionById(10L)).thenReturn(transaction);
        when(transactionService.toResponseDTO(transaction)).thenReturn(response);

        mockMvc.perform(get("/transaction/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(10.50));
    }

    @Test
    void getTransactionById_withUnknownId_shouldReturn404() throws Exception {
        when(transactionService.findTransactionById(99L))
                .thenThrow(new TransactionNotFound("Transaction not found"));

        mockMvc.perform(get("/transaction/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    @Test
    void createTransaction_whenServiceRejectsAmount_shouldReturn400() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new InvalidAmount("Transaction amount must be greater than zero"));

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DEPOSIT\",\"receiverId\":1,\"amount\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Transaction amount must be greater than zero"));
    }

    @Test
    void createTransaction_withSameSenderAndReceiver_shouldReturn400() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new SameAccountTransfer("Sender and receiver must be different accounts"));

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TRANSFER\",\"senderId\":1,\"receiverId\":1,\"amount\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Sender and receiver must be different accounts"));
    }

    @Test
    void createTransaction_whenAccountWasChangedConcurrently_shouldReturn409() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, 1L));

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"WITHDRAWAL\",\"senderId\":1,\"amount\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("The account was changed by another operation, please try again"));
    }

    @Test
    void createTransaction_whenDatabaseDetectsDeadlock_shouldReturn409() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new CannotAcquireLockException("deadlock detected"));

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TRANSFER\",\"senderId\":1,\"receiverId\":2,\"amount\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("The account was changed by another operation, please try again"));
    }
}
