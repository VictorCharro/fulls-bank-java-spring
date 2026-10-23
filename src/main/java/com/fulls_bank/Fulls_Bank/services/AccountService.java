package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.AccountRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNumberUnavailable;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.mappers.AccountMapper;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import com.fulls_bank.Fulls_Bank.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    static final int MIN_ACCOUNT_NUMBER = 100_000;
    static final int MAX_ACCOUNT_NUMBER = 999_999;
    static final int MAX_NUMBER_ATTEMPTS = 20;

    public final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;

    public Integer generateUniqueAccountNumber() {
        Random random = new Random();
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            int number = MIN_ACCOUNT_NUMBER + random.nextInt(MAX_ACCOUNT_NUMBER - MIN_ACCOUNT_NUMBER + 1);
            if (!accountRepository.existsByAccountNumber(number)) {
                return number;
            }
        }
        throw new AccountNumberUnavailable("Could not generate a unique account number, try again later");
    }

    public Account findAccountByAccountNumber (int accountNumber) {
        return accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFound("Account not found"));
    }

    public Account findAccountById (Long id) {
        return accountRepository.findAccountById(id)
                .orElseThrow(() -> new IdNotFound("ID not found"));
    }

    public void deleteAccount(Long id) {
        findAccountById(id);
        if (transactionRepository.existsBySenderIdOrReceiverId(id, id)) {
            throw new ResourceInUse("Account has transactions and cannot be deleted");
        }
        try {
            accountRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUse("Account has transactions and cannot be deleted");
        }
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
        return accountMapper.toResponse(saved);
    }

}