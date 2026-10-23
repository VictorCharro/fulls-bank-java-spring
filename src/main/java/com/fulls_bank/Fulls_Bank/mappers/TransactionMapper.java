package com.fulls_bank.Fulls_Bank.mappers;

import com.fulls_bank.Fulls_Bank.dto.TransactionResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Converte {@link Transaction} em {@link TransactionResponseDTO}. A origem (depósito) ou o destino (saque)
 * ausentes viram {@code null} no DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransactionMapper {

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "receiverId", source = "receiver.id")
    TransactionResponseDTO toResponse(Transaction transaction);
}
