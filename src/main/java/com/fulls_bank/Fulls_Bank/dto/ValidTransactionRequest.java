package com.fulls_bank.Fulls_Bank.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Exige as contas que o tipo da transação usa: destino no depósito, origem no saque, ambas na transferência. */
@Documented
@Constraint(validatedBy = TransactionRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransactionRequest {

    String message() default "Invalid transaction request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
