package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.TransactionRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Transações", description = "Depósito, saque e transferência entre contas.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Listar todas as transações", description = "Somente ADMIN. Para o histórico de uma conta, use o extrato.")
    @ApiResponse(responseCode = "200", description = "Lista de transações")
    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    @GetMapping
    public List<TransactionResponseDTO> transactionList() {
        return transactionService.listTransactions()
                .stream()
                .map(transactionService::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Buscar transação por id")
    @ApiResponse(responseCode = "200", description = "Transação encontrada")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public TransactionResponseDTO getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.findTransactionById(id);
        return transactionService.toResponseDTO(transaction);
    }

    @Operation(summary = "Criar transação (depósito, saque ou transferência)",
            description = "`DEPOSIT` usa só `receiverId`; `WITHDRAWAL` usa só `senderId`; `TRANSFER` usa os dois. "
                    + "A operação é atômica: se uma regra falhar, nenhum saldo muda. Em conflito de concorrência "
                    + "a resposta é 409 e a operação pode ser repetida.")
    @ApiResponse(responseCode = "201", description = "Transação registrada e saldos atualizados")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO dto) {
        Transaction transaction = transactionService.createTransaction(dto);
        TransactionResponseDTO responseDTO = transactionService.toResponseDTO(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
