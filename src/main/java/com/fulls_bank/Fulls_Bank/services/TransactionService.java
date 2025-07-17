package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.TransactionDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.InsufficientBalance;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    public final TransactionRepository transactionRepository;
    public final AccountRepository accountRepository;

    public List<Transaction> listTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> findTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional
    public Transaction createTransaction(TransactionDTO dto) {

        Account sender = null;
        Account receiver = null;
        TransactionType type = dto.getType();

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        switch (type) {
            case DEPOSIT:
                receiver = accountRepository.findById(dto.getReceiverId())
                        .orElseThrow(() -> new AccountNotFound("Receiver account not found"));

                receiver.setBalance(receiver.getBalance().add(dto.getAmount()));
                accountRepository.save(receiver);
                break;

            case WITHDRAWAL:
                sender = accountRepository.findById(dto.getSenderId())
                        .orElseThrow(() -> new AccountNotFound("Sender account not found"));

                if (sender.getBalance().compareTo(dto.getAmount()) < 0) {
                    throw new InsufficientBalance("Sender has insufficient balance");
                }

                sender.setBalance(sender.getBalance().subtract(dto.getAmount()));
                accountRepository.save(sender);
                break;

            case TRANSFER:
                sender = accountRepository.findById(dto.getSenderId())
                        .orElseThrow(() -> new AccountNotFound("Sender account not found"));

                receiver = accountRepository.findById(dto.getReceiverId())
                        .orElseThrow(() -> new AccountNotFound("Receiver account not found"));

                if (sender.getBalance().compareTo(dto.getAmount()) < 0) {
                    throw new InsufficientBalance("Sender has insufficient balance");
                }
                if (sender.getId().equals(receiver.getId())) {
                    throw new IllegalArgumentException("Sender and receiver must be different accounts");
                }

                sender.setBalance(sender.getBalance().subtract(dto.getAmount()));
                receiver.setBalance(receiver.getBalance().add(dto.getAmount()));

                accountRepository.save(sender);
                accountRepository.save(receiver);
                break;
        }

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(dto.getAmount());
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }
}
