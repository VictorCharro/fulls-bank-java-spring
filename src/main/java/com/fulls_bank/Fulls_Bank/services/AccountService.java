package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.InsufficientBalance;
import com.fulls_bank.Fulls_Bank.exceptions.NegativeAmount;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    public final AccountRepository accountRepository;

    public Account createAccount(Account account) {
        if (account.getClient() == null) {
            throw new IllegalArgumentException("The Client is mandatory");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeAmount("The balance cannot be negative");
        }
        return accountRepository.save(account);
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
}