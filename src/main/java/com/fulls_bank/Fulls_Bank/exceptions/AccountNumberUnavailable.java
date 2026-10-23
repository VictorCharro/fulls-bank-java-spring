package com.fulls_bank.Fulls_Bank.exceptions;

public class AccountNumberUnavailable extends RuntimeException {
    public AccountNumberUnavailable(String message) {
        super(message);
    }
}
