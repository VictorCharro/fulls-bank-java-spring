package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.AccountRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidPagination;
import com.fulls_bank.Fulls_Bank.services.AccountService;
import com.fulls_bank.Fulls_Bank.services.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountRequestDTO dto) {
        AccountResponseDTO response = accountService.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public AccountResponseDTO searchAccountById(@PathVariable Long id) {
        Account account = accountService.findAccountById(id);
        return accountService.toResponseDTO(account);
    }

    @GetMapping("/number/{number}")
    public AccountResponseDTO searchAccountByNumber(@PathVariable int number) {
        Account account = accountService.findAccountByAccountNumber(number);
        return accountService.toResponseDTO(account);
    }

    @GetMapping("/{id}/statement")
    public Page<TransactionResponseDTO> statement(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Pageable pageable) {
        // O resolvedor de Pageable do Spring converte page negativo em 0 e size <= 0 no padrão sem avisar;
        // aqui valor explicitamente inválido vira 400. Ausente, valem os padrões.
        if ((page != null && page < 0) || (size != null && size < 1)) {
            throw new InvalidPagination("page must be 0 or greater and size must be at least 1");
        }
        return transactionService.getStatement(id, from, to, pageable).map(transactionService::toResponseDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
