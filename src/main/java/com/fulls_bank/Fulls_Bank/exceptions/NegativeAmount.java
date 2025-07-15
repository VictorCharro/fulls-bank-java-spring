package com.fulls_bank.Fulls_Bank.exceptions;

public class NegativeAmount extends RuntimeException {
    public NegativeAmount(String message) {
        super(message);
    }
}
