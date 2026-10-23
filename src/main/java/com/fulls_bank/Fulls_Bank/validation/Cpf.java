package com.fulls_bank.Fulls_Bank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF válido: 11 dígitos (com ou sem a máscara {@code 000.000.000-00}), dígitos verificadores corretos e nem
 * todos os dígitos iguais. Valor nulo é aceito; use {@code @NotBlank} junto para exigir o campo.
 */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cpf {

    String message() default "CPF is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
