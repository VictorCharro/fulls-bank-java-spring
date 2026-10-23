package com.fulls_bank.Fulls_Bank.exceptions;

public class TransactionNotFound extends RuntimeException {
    public TransactionNotFound(String message) {
        super(message);
    }
}
