package com.fulls_bank.Fulls_Bank.mappers;

import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Converte {@link Account} em {@link AccountResponseDTO}. Campo novo no DTO sem mapeamento ou {@code ignore}
 * explícito falha a compilação ({@code unmappedTargetPolicy = ERROR}).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper {

    // As listas de transações ficam fora da resposta da conta: o histórico é lido pelo extrato
    // (GET /account/{id}/statement), e mapeá-las aqui carregaria coleções lazy do banco sem necessidade.
    @Mapping(target = "sentTransactions", ignore = true)
    @Mapping(target = "receivedTransactions", ignore = true)
    AccountResponseDTO toResponse(Account account);
}
