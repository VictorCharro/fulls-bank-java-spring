package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.AccountRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidPagination;
import com.fulls_bank.Fulls_Bank.services.AccountService;
import com.fulls_bank.Fulls_Bank.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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

@Tag(name = "Contas", description = "Abertura, consulta, extrato e exclusão de contas.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Operation(summary = "Abrir conta",
            description = "A conta nasce com saldo zero e um número de 6 dígitos gerado automaticamente.")
    @ApiResponse(responseCode = "201", description = "Conta aberta")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "503", ref = "#/components/responses/ServiceUnavailable")
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountRequestDTO dto) {
        AccountResponseDTO response = accountService.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar conta por id")
    @ApiResponse(responseCode = "200", description = "Conta encontrada")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public AccountResponseDTO searchAccountById(@PathVariable Long id) {
        Account account = accountService.findAccountById(id);
        return accountService.toResponseDTO(account);
    }

    @Operation(summary = "Buscar conta pelo número")
    @ApiResponse(responseCode = "200", description = "Conta encontrada")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/number/{number}")
    public AccountResponseDTO searchAccountByNumber(@PathVariable int number) {
        Account account = accountService.findAccountByAccountNumber(number);
        return accountService.toResponseDTO(account);
    }

    @Operation(summary = "Extrato da conta",
            description = "Transações em que a conta é origem ou destino, da mais recente para a mais antiga. "
                    + "O período é opcional e inclusivo nas duas pontas. A ordem é fixa: o parâmetro `sort` é ignorado. "
                    + "Tamanho de página padrão 20, máximo 100.")
    @ApiResponse(responseCode = "200", description = "Página do extrato")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}/statement")
    public PagedModel<TransactionResponseDTO> statement(
            @PathVariable Long id,
            @Parameter(description = "Início do período (inclusivo)", example = "2026-03-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fim do período (inclusivo)", example = "2026-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Número da página, começando em 0", example = "0")
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "Itens por página (1 a 100)", example = "20")
            @RequestParam(name = "size", required = false) Integer size,
            @Parameter(hidden = true) Pageable pageable) {
        // O resolvedor de Pageable do Spring converte page negativo em 0 e size <= 0 no padrão sem avisar;
        // aqui valor explicitamente inválido vira 400. Ausente, valem os padrões.
        if ((page != null && page < 0) || (size != null && size < 1)) {
            throw new InvalidPagination("page must be 0 or greater and size must be at least 1");
        }
        Page<TransactionResponseDTO> result = transactionService.getStatement(id, from, to, pageable)
                .map(transactionService::toResponseDTO);
        return new PagedModel<>(result);
    }

    @Operation(summary = "Excluir conta", description = "Somente ADMIN. Bloqueado se a conta tiver transações.")
    @ApiResponse(responseCode = "200", description = "Conta excluída")
    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
