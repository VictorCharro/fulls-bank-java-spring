package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.ClientResponseDTO;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.services.ClientService;
import com.fulls_bank.Fulls_Bank.security.ApiSecurityTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@ApiSecurityTest
@WithMockUser(roles = "ADMIN")
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

    private static final ClientResponseDTO ANA = new ClientResponseDTO(1L, "12345678901", "Ana", "Silva");

    @Test
    void createClient_shouldIgnoreIdInBodyAndReturnDto() throws Exception {
        when(clientService.createClient(any())).thenReturn(ANA);

        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"cpf\":\"12345678901\",\"name\":\"Ana\",\"lastName\":\"Silva\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Silva"));
    }

    @Test
    void createClient_withInvalidBody_shouldReturn400AndNotCallService() throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"123\",\"name\":\" \",\"lastName\":\"Silva\"}"))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"name\":\"Ana\",\"lastName\":\"Silva\"}",
            "{\"cpf\":\"\",\"name\":\"Ana\",\"lastName\":\"Silva\"}",
            "{\"cpf\":\"1234567890a\",\"name\":\"Ana\",\"lastName\":\"Silva\"}",
            "{\"cpf\":\"123456789012\",\"name\":\"Ana\",\"lastName\":\"Silva\"}"})
    void createClient_withMissingOrMalformedCpf_shouldReturn400AndNotCallService(String body) throws Exception {
        mockMvc.perform(post("/client").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    void listClients_shouldReturnDtos() throws Exception {
        when(clientService.listClients()).thenReturn(List.of(ANA));

        mockMvc.perform(get("/client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ana"));
    }

    @Test
    void updateClient_shouldAcceptOnlyNameAndLastName() throws Exception {
        when(clientService.updateClient(any(), any())).thenReturn(ANA);

        mockMvc.perform(put("/client/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"lastName\":\"Silva\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"));

        mockMvc.perform(put("/client/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"lastName\":\"Silva\"}"))
                .andExpect(status().isBadRequest());
    }
}
