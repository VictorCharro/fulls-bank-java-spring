package com.fulls_bank.Fulls_Bank.repositories;

import com.fulls_bank.Fulls_Bank.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    public Optional<Client> findClientById(Long id);
    public Optional<Client> findClientByCpf(String cpf);
}
