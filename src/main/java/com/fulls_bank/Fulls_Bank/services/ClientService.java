package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.ClientRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientUpdateDTO;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ExistingAccount;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.mappers.ClientMapper;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import com.fulls_bank.Fulls_Bank.validation.CpfValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    public final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final ClientMapper clientMapper;

    public ClientResponseDTO createClient(ClientRequestDTO dto) {
        if (clientRepository.findClientByCpf(CpfValidator.normalize(dto.getCpf())).isPresent()){
            throw new ExistingAccount("An account with this CPF already exists");
        }
        return clientMapper.toResponse(clientRepository.save(clientMapper.toEntity(dto)));
    }

    public ClientResponseDTO searchClient(Long id) {
        return clientMapper.toResponse(findClient(id));
    }

    private Client findClient(Long id) {
        return clientRepository.findClientById(id)
                .orElseThrow(() -> new ClientNotFound("Client not found"));
    }

    public void deleteClient(Long id) {
        findClient(id);
        if (accountRepository.existsByClientId(id)) {
            throw new ResourceInUse("Client has accounts and cannot be deleted");
        }
        try {
            clientRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUse("Client has accounts and cannot be deleted");
        }
    }

    public List<ClientResponseDTO> listClients() {
        return clientRepository.findAll().stream().map(clientMapper::toResponse).toList();
    }

    public ClientResponseDTO updateClient(Long id, ClientUpdateDTO dto) {
        Client client = findClient(id);
        client.setName(dto.getName());
        client.setLastName(dto.getLastName());
        return clientMapper.toResponse(clientRepository.save(client));
    }
}