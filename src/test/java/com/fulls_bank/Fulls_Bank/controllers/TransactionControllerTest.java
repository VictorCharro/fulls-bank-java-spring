package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
}
