package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ExistingAccount;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    public final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    public Client createClient(Client client) {
        if (clientRepository.findClientByCpf(client.getCpf()).isPresent()){
            throw new ExistingAccount("An account with this CPF already exists");
        }
        return clientRepository.save(client);
    }

    public Client searchClient(Long id) {
        return clientRepository.findClientById(id)
                .orElseThrow(() -> new ClientNotFound("Client not found"));
    }

    public void deleteClient(Long id) {
        searchClient(id);
        if (accountRepository.existsByClientId(id)) {
            throw new ResourceInUse("Client has accounts and cannot be deleted");
        }
        try {
            clientRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUse("Client has accounts and cannot be deleted");
        }
    }

    public List<Client> listClients() {
        return clientRepository.findAll();
    }

    public Client updateClient(Long id, Client client) {
        Client clientTemp = searchClient(id);
        clientTemp.setName(client.getName());
        clientTemp.setLastName(client.getLastName());
        return clientRepository.save(clientTemp);
    }
}