package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.TransactionRequestDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.InsufficientBalance;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidAmount;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidPeriod;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.SameAccountTransfer;
import com.fulls_bank.Fulls_Bank.exceptions.TransactionNotFound;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    public final TransactionRepository transactionRepository;
    public final AccountRepository accountRepository;

    public List<Transaction> listTransactions() {
        return transactionRepository.findAll();
    }

    // Mais recentes primeiro; o id desempata transações com o mesmo instante, mantendo a paginação estável
    private static final Sort STATEMENT_ORDER = Sort.by(Sort.Direction.DESC, "timestamp", "id");

    /**
     * Extrato de uma conta: transações em que ela é origem ou destino, com período opcional (ambas as datas
     * inclusivas) e paginação. A ordenação é sempre a do extrato; a pedida pelo cliente é ignorada.
     */
    public Page<Transaction> getStatement(Long accountId, LocalDate from, LocalDate to, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new IdNotFound("Account not found");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidPeriod("The start date must not be after the end date");
        }

        Specification<Transaction> spec = (root, query, cb) -> cb.or(
                cb.equal(root.get("sender").get("id"), accountId),
                cb.equal(root.get("receiver").get("id"), accountId));
        if (from != null) {
            LocalDateTime start = from.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), start));
        }
        if (to != null) {
            LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("timestamp"), endExclusive));
        }

        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), STATEMENT_ORDER);
        return transactionRepository.findAll(spec, page);
    }

    public Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFound("Transaction not found"));
    }

    @Transactional
    public Transaction createTransaction(TransactionRequestDTO dto) {

        Account sender = null;
        Account receiver = null;
        TransactionType type = dto.getType();

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmount("Transaction amount must be greater than zero");
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
                    throw new SameAccountTransfer("Sender and receiver must be different accounts");
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
