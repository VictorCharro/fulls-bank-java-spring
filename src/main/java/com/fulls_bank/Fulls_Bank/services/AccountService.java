package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.AccountRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.InsufficientBalance;
import com.fulls_bank.Fulls_Bank.exceptions.NegativeAmount;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    public final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    public Integer generateUniqueAccountNumber() {
        Random random = new Random();
        Integer number;
        do {
            number = 1000 + random.nextInt(9000); // número entre 1000 e 9999
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    public Account findAccountByAccountNumber (int accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFound("Account not found"));
    }

    public Account findAccountById (Long id) {
        return accountRepository.findAccountById(id)
                .orElseThrow(() -> new IdNotFound("ID not found"));
    }

    @Transactional
    public void deposit(int accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeAmount("The amount cannot be negative");
        }
        Account account = findAccountByAccountNumber(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void withdrawal(int accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeAmount("The amount cannot be negative");
        }
        Account account = findAccountByAccountNumber(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalance("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        findAccountById(id);
        accountRepository.deleteById(id);
    }

    public AccountResponseDTO createAccount(AccountRequestDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ClientNotFound("Client not found"));

        Account account = new Account();
        account.setType(dto.getType());
        account.setClient(client);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        Account saved = accountRepository.save(account);
        return toResponseDTO(saved);
    }

    public AccountResponseDTO toResponseDTO(Account account) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setType(account.getType());
        dto.setBalance(account.getBalance());
        return dto;
    }

}