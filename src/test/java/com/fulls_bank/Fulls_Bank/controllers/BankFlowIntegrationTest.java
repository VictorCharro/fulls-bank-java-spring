package com.fulls_bank.Fulls_Bank.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BankFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_createClientAndAccounts_depositWithdrawTransfer_shouldUpdateBalances() throws Exception {
        long clientId = createClient("12345678901");
        int accountNumberA = createAccount(clientId);
        int accountNumberB = createAccount(clientId);
        long accountIdA = accountRepository.findAccountByAccountNumber(accountNumberA).orElseThrow().getId();
        long accountIdB = accountRepository.findAccountByAccountNumber(accountNumberB).orElseThrow().getId();

        mockMvc.perform(get("/account/number/{number}", accountNumberA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.0));

        postTransaction("{\"type\":\"DEPOSIT\",\"receiverId\":" + accountIdA + ",\"amount\":100.00}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.receiverId").value(accountIdA));

        postTransaction("{\"type\":\"WITHDRAWAL\",\"senderId\":" + accountIdA + ",\"amount\":30.00}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));

        postTransaction("{\"type\":\"TRANSFER\",\"senderId\":" + accountIdA + ",\"receiverId\":" + accountIdB
                + ",\"amount\":50.00}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.senderId").value(accountIdA))
                .andExpect(jsonPath("$.receiverId").value(accountIdB));

        mockMvc.perform(get("/account/number/{number}", accountNumberA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(20.0));
        mockMvc.perform(get("/account/number/{number}", accountNumberB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.0));

        mockMvc.perform(get("/transaction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.type == 'DEPOSIT')].amount").value(100.0))
                .andExpect(jsonPath("$[?(@.type == 'DEPOSIT')].receiverId").value((int) accountIdA))
                .andExpect(jsonPath("$[?(@.type == 'WITHDRAWAL')].amount").value(30.0))
                .andExpect(jsonPath("$[?(@.type == 'WITHDRAWAL')].senderId").value((int) accountIdA))
                .andExpect(jsonPath("$[?(@.type == 'TRANSFER')].amount").value(50.0))
                .andExpect(jsonPath("$[?(@.type == 'TRANSFER')].senderId").value((int) accountIdA))
                .andExpect(jsonPath("$[?(@.type == 'TRANSFER')].receiverId").value((int) accountIdB));
        assertEquals(3, transactionRepository.count());
    }

    @Test
    void fullFlow_deleteAccountAndClient_shouldBeBlockedWhileTheyAreInUse() throws Exception {
        long clientId = createClient("12345678901");
        int accountNumber = createAccount(clientId);
        long accountId = accountRepository.findAccountByAccountNumber(accountNumber).orElseThrow().getId();
        postTransaction("{\"type\":\"DEPOSIT\",\"receiverId\":" + accountId + ",\"amount\":10.00}")
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/account/{id}", accountId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/client/{id}", clientId)).andExpect(status().isConflict());

        assertEquals(1, accountRepository.count());
        assertEquals(1, clientRepository.count());
    }

    private long createClient(String cpf) throws Exception {
        String body = "{\"cpf\":\"" + cpf + "\",\"name\":\"Ana\",\"lastName\":\"Silva\"}";
        String response = mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private int createAccount(long clientId) throws Exception {
        String body = "{\"type\":\"CHECKING\",\"clientId\":" + clientId + "}";
        String response = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0.0))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accountNumber").asInt();
    }

    private org.springframework.test.web.servlet.ResultActions postTransaction(String body) throws Exception {
        return mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
