package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.ClientRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientUpdateDTO;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ExistingAccount;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.mappers.ClientMapper;
import com.fulls_bank.Fulls_Bank.mappers.ClientMapperImpl;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final Long CLIENT_ID = 1L;
    private static final String CPF = "52998224725";

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountRepository accountRepository;

    @Spy
    private ClientMapper clientMapper = new ClientMapperImpl();

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
    void createClient_withNewCpf_shouldSaveAndReturnDto() {
        when(clientRepository.findClientByCpf(CPF)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponseDTO result = clientService.createClient(new ClientRequestDTO(CPF, "Maria", "Silva"));

        assertEquals(CLIENT_ID, result.getId());
        assertEquals(CPF, result.getCpf());
        assertEquals("Maria", result.getName());
        assertEquals("Silva", result.getLastName());
        verify(clientRepository).save(argThat(saved -> saved.getId() == null && CPF.equals(saved.getCpf())));
    }

    @Test
    void createClient_withMaskedCpf_shouldLookUpAndSaveOnlyDigits() {
        when(clientRepository.findClientByCpf(CPF)).thenReturn(Optional.of(client));

        assertThrows(ExistingAccount.class,
                () -> clientService.createClient(new ClientRequestDTO("529.982.247-25", "Maria", "Silva")));
    }

    @Test
    void createClient_withExistingCpf_shouldThrowAndNotSave() {
        when(clientRepository.findClientByCpf(CPF)).thenReturn(Optional.of(client));

        assertThrows(ExistingAccount.class,
                () -> clientService.createClient(new ClientRequestDTO(CPF, "Maria", "Silva")));

        verify(clientRepository, never()).save(any());
    }

    @Test
    void searchClient_withExistingId_shouldReturnDto() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));

        ClientResponseDTO result = clientService.searchClient(CLIENT_ID);

        assertEquals(CLIENT_ID, result.getId());
        assertEquals(CPF, result.getCpf());
    }

    @Test
    void searchClient_withUnknownId_shouldThrowClientNotFound() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> clientService.searchClient(CLIENT_ID));
    }

    @Test
    void deleteClient_withExistingId_shouldDelete() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(accountRepository.existsByClientId(CLIENT_ID)).thenReturn(false);

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
    void deleteClient_withLinkedAccounts_shouldThrowResourceInUseAndNotDelete() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(accountRepository.existsByClientId(CLIENT_ID)).thenReturn(true);

        assertThrows(ResourceInUse.class, () -> clientService.deleteClient(CLIENT_ID));

        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void deleteClient_whenForeignKeyViolationOnDelete_shouldThrowResourceInUse() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(accountRepository.existsByClientId(CLIENT_ID)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("fk")).when(clientRepository).deleteById(CLIENT_ID);

        assertThrows(ResourceInUse.class, () -> clientService.deleteClient(CLIENT_ID));
    }

    @Test
    void listClients_shouldReturnAllClientsAsDtos() {
        when(clientRepository.findAll()).thenReturn(List.of(client));

        List<ClientResponseDTO> result = clientService.listClients();

        assertEquals(1, result.size());
        assertEquals(CPF, result.get(0).getCpf());
    }

    @Test
    void updateClient_shouldChangeOnlyNameAndLastName() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);

        ClientResponseDTO result = clientService.updateClient(CLIENT_ID, new ClientUpdateDTO("Ana", "Souza"));

        assertEquals("Ana", result.getName());
        assertEquals("Souza", result.getLastName());
        assertEquals(CPF, result.getCpf());
    }

    @Test
    void updateClient_withUnknownId_shouldThrowAndNotSave() {
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> clientService.updateClient(CLIENT_ID, new ClientUpdateDTO("Ana", "Souza")));

        verify(clientRepository, never()).save(any());
    }
}
