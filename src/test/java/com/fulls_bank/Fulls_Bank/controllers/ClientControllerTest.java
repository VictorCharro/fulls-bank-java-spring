package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.services.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @Test
    void deleteClient_withLinkedAccounts_shouldReturn409() throws Exception {
        doThrow(new ResourceInUse("Client has accounts and cannot be deleted"))
                .when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/client/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Client has accounts and cannot be deleted"));
    }
}
