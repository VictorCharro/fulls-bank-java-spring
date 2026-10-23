package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ExistingAccount;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final Long CLIENT_ID = 1L;
    private static final String CPF = "12345678901";

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(CLIENT_ID);
        client.setCpf(CPF);
        client.setName("Maria");
        client.setLastName("Silva");
    }

    @Test
    void createClient_withNewCpf_shouldSaveAndReturnClient() {
        when(clientRepository.findClientByCpf(CPF)).thenReturn(Optional.empty());
        when(clientRepository.save(client)).thenReturn(client);

        Client result = clientService.createClient(client);

        assertSame(client, result);
        verify(clientRepository).save(client);
    }

    @Test
    void createClient_withExistingCpf_shouldThrowAndNotSave() {
        when(clientRepository.findClientByCpf(CPF)).thenReturn(Optional.of(client));

        assertThrows(ExistingAccount.class, () -> clientService.createClient(client));

        verify(clientRepository, never()).save(any());
    }

    @Test
    void searchClient_withExistingId_shouldReturnClient() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));

        assertSame(client, clientService.searchClient(CLIENT_ID));
    }

    @Test
    void searchClient_withUnknownId_shouldThrowClientNotFound() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> clientService.searchClient(CLIENT_ID));
    }

    @Test
    void deleteClient_withExistingId_shouldDelete() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));

        clientService.deleteClient(CLIENT_ID);

        verify(clientRepository).deleteById(CLIENT_ID);
    }

    @Test
    void deleteClient_withUnknownId_shouldThrowAndNotDelete() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> clientService.deleteClient(CLIENT_ID));

        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void listClients_shouldReturnAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(client));

        assertEquals(List.of(client), clientService.listClients());
    }

    @Test
    void updateClient_shouldChangeOnlyNameAndLastName() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);

        Client changes = new Client();
        changes.setCpf("99999999999");
        changes.setName("Ana");
        changes.setLastName("Souza");

        Client result = clientService.updateClient(CLIENT_ID, changes);

        assertEquals("Ana", result.getName());
        assertEquals("Souza", result.getLastName());
        assertEquals(CPF, result.getCpf());
    }

    @Test
    void updateClient_withUnknownId_shouldThrowAndNotSave() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> clientService.updateClient(CLIENT_ID, client));

        verify(clientRepository, never()).save(any());
    }
}
