package com.fulls_bank.Fulls_Bank.exceptions;

public class ExistingAccount extends RuntimeException {
    public ExistingAccount(String message) {
        super(message);
    }
}
