package com.fulls_bank.Fulls_Bank.exceptions;

public class EmailAlreadyRegistered extends RuntimeException {
    public EmailAlreadyRegistered(String message) {
        super(message);
    }
}
