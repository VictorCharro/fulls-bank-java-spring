package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorResponsesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private Long accountA;
    private Long accountB;

    @BeforeEach
    void createAccounts() {
        Client client = new Client();
        client.setCpf("52998224725");
        client.setName("Ana");
        client.setLastName("Silva");
        client = clientRepository.save(client);
        accountA = saveAccount(client, 1001, "100.00");
        accountB = saveAccount(client, 1002, "0.00");
    }

    @Test
    void withdrawal_withInsufficientBalance_shouldReturn400AndKeepBalance() throws Exception {
        postTransaction("{\"type\":\"WITHDRAWAL\",\"senderId\":" + accountA + ",\"amount\":500.00}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(accountA)));
        assertEquals(0, transactionRepository.count());
    }

    @Test
    void transaction_withZeroAmount_shouldReturn400() throws Exception {
        postTransaction("{\"type\":\"DEPOSIT\",\"receiverId\":" + accountA + ",\"amount\":0}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_toTheSameAccount_shouldReturn400() throws Exception {
        postTransaction("{\"type\":\"TRANSFER\",\"senderId\":" + accountA + ",\"receiverId\":" + accountA
                + ",\"amount\":10.00}")
                .andExpect(status().isBadRequest());

        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(accountA)));
    }

    @Test
    void transfer_withUnknownReceiver_shouldReturn404AndKeepBalances() throws Exception {
        postTransaction("{\"type\":\"TRANSFER\",\"senderId\":" + accountA + ",\"receiverId\":999999,\"amount\":10.00}")
                .andExpect(status().isNotFound());

        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(accountA)));
        assertEquals(0, transactionRepository.count());
    }

    @Test
    void transaction_withMissingType_shouldReturn400() throws Exception {
        postTransaction("{\"receiverId\":" + accountB + ",\"amount\":10.00}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void createClient_withDuplicateCpf_shouldReturn400() throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"52998224725\",\"name\":\"Bia\",\"lastName\":\"Souza\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(1, clientRepository.count());
    }

    @Test
    void createClient_withMaskedCpfOfAnExistingClient_shouldReturn400() throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"529.982.247-25\",\"name\":\"Bia\",\"lastName\":\"Souza\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(1, clientRepository.count());
    }

    @Test
    void createClient_withMaskedCpf_shouldStoreOnlyDigits() throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"111.444.777-35\",\"name\":\"Caio\",\"lastName\":\"Lima\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("11144477735"));

        assertEquals(1, clientRepository.findAll().stream().filter(c -> "11144477735".equals(c.getCpf())).count());
    }

    @Test
    void createClient_withCpfWithWrongCheckDigits_shouldReturn400() throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"12345678901\",\"name\":\"Bia\",\"lastName\":\"Souza\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createClient_withInvalidCpf_shouldReturn400() throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"123\",\"name\":\"Bia\",\"lastName\":\"Souza\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_withUnknownClient_shouldReturn404() throws Exception {
        mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CHECKING\",\"clientId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccount_withUnknownNumber_shouldReturn404() throws Exception {
        mockMvc.perform(get("/account/number/{number}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransaction_withUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/transaction/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    private ResultActions postTransaction(String body) throws Exception {
        return mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private Long saveAccount(Client client, int number, String balance) {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setBalance(new BigDecimal(balance));
        account.setType(AccountType.CHECKING);
        account.setClient(client);
        return accountRepository.save(account).getId();
    }

    private BigDecimal balanceOf(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow().getBalance();
    }
}
