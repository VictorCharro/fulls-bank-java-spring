package com.fulls_bank.Fulls_Bank.mappers;

import com.fulls_bank.Fulls_Bank.dto.ClientRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.validation.CpfValidator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Converte entre {@link Client} e os DTOs de cliente. Campo novo sem mapeamento ou {@code ignore} explícito
 * falha a compilação ({@code unmappedTargetPolicy = ERROR}).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, imports = CpfValidator.class)
public interface ClientMapper {

    ClientResponseDTO toResponse(Client client);

    // O id é gerado pelo banco; nunca vem do corpo da requisição
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", expression = "java(CpfValidator.normalize(dto.getCpf()))")
    Client toEntity(ClientRequestDTO dto);
}
