package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.TransactionDTO;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<Transaction> transactionList() {
        return transactionService.listTransactions();
    }

    @GetMapping("/{id}")
    public Optional<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionService.findTransactionById(id);
    }

    @PostMapping
    public Transaction createTransaction(@Valid @RequestBody TransactionDTO dto) {
        return transactionService.createTransaction(dto);
    }
}
