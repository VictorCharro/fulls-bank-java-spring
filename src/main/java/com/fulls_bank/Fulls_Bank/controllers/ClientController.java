package com.fulls_bank.Fulls_Bank.controllers;

import com.fulls_bank.Fulls_Bank.dto.ClientRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientResponseDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientUpdateDTO;
import com.fulls_bank.Fulls_Bank.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Clientes", description = "Cadastro e manutenção de clientes.")
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Criar cliente", description = "O CPF deve ser válido, com ou sem máscara, e ser único.")
    @ApiResponse(responseCode = "200", description = "Cliente criado")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @PostMapping
    public ClientResponseDTO createClient(@Valid @RequestBody ClientRequestDTO dto) {
        return clientService.createClient(dto);
    }

    @Operation(summary = "Listar clientes", description = "Somente ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    @GetMapping
    public List<ClientResponseDTO> listClients() {
        return clientService.listClients();
    }

    @Operation(summary = "Buscar cliente por id")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @GetMapping("/{id}")
    public ClientResponseDTO searchClient(@PathVariable Long id) {
        return clientService.searchClient(id);
    }

    @Operation(summary = "Excluir cliente", description = "Somente ADMIN. Bloqueado se o cliente tiver contas.")
    @ApiResponse(responseCode = "200", description = "Cliente excluído")
    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
    }

    @Operation(summary = "Atualizar cliente", description = "Altera somente nome e sobrenome; o CPF não muda.")
    @ApiResponse(responseCode = "200", description = "Cliente atualizado")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @PutMapping("/{id}")
    public ClientResponseDTO updateClient(@PathVariable Long id, @Valid @RequestBody ClientUpdateDTO dto) {
        return clientService.updateClient(id, dto);
    }

}