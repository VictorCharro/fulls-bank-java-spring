package com.fulls_bank.Fulls_Bank.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionRequestValidator implements ConstraintValidator<ValidTransactionRequest, TransactionRequestDTO> {

    @Override
    public boolean isValid(TransactionRequestDTO dto, ConstraintValidatorContext context) {
        // Tipo ausente já é recusado pelo @NotNull do próprio campo
        if (dto == null || dto.getType() == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;
        switch (dto.getType()) {
            case DEPOSIT -> valid = require(context, dto.getReceiverId(), "receiverId", "deposits");
            case WITHDRAWAL -> valid = require(context, dto.getSenderId(), "senderId", "withdrawals");
            case TRANSFER -> {
                boolean sender = require(context, dto.getSenderId(), "senderId", "transfers");
                boolean receiver = require(context, dto.getReceiverId(), "receiverId", "transfers");
                valid = sender && receiver;
            }
        }
        return valid;
    }

    private static boolean require(ConstraintValidatorContext context, Long id, String field, String operation) {
        if (id != null) {
            return true;
        }
        context.buildConstraintViolationWithTemplate("must be informed for " + operation)
                .addPropertyNode(field).addConstraintViolation();
        return false;
    }
}
