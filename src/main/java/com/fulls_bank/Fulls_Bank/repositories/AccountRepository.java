package com.fulls_bank.Fulls_Bank.repositories;

import com.fulls_bank.Fulls_Bank.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    public Optional<Account> findAccountById(Long id);
    public Optional<Account> findByAccountNumber(int AccountNumber);
}
