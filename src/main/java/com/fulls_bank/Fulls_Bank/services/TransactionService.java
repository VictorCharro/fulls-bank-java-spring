package com.fulls_bank.Fulls_Bank.services;

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
    public Transaction createTransaction(Transaction transaction) {

        Account sender;
        Account receiver;
        TransactionType type = transaction.getType();

        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        switch (type) {
            case DEPOSIT:
                receiver = accountRepository.findById(transaction.getReceiver().getId())
                        .orElseThrow(() -> new AccountNotFound("Receiver account not found"));

                receiver.setBalance(receiver.getBalance().add(transaction.getAmount()));
                accountRepository.save(receiver);
                break;

            case WITHDRAWAL:
                sender = accountRepository.findById(transaction.getSender().getId())
                        .orElseThrow(() -> new AccountNotFound("Sender account not found"));

                if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {
                    throw new InsufficientBalance("Sender has insufficient balance");
                }

                sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));
                accountRepository.save(sender);
                break;

            case TRANSFER:
                sender = accountRepository.findById(transaction.getSender().getId())
                        .orElseThrow(() -> new AccountNotFound("Sender account not found"));

                receiver = accountRepository.findById(transaction.getReceiver().getId())
                        .orElseThrow(() -> new AccountNotFound("Receiver account not found"));

                if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {
                    throw new InsufficientBalance("Sender has insufficient balance");
                }
                if (sender.getId().equals(receiver.getId())) {
                    throw new IllegalArgumentException("Sender and receiver must be different accounts");
                }

                sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));
                receiver.setBalance(receiver.getBalance().add(transaction.getAmount()));

                accountRepository.save(sender);
                accountRepository.save(receiver);
                break;
        }

        return transactionRepository.save(transaction);
    }
}
