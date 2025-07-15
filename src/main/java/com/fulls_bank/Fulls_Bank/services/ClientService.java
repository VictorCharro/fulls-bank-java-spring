package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ExistingAccount;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    public final ClientRepository clientRepository;

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
        clientRepository.deleteById(id);
    }

    public List<Client> listClients() {
        return clientRepository.findAll();
    }
}