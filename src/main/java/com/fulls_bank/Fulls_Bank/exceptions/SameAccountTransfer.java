package com.fulls_bank.Fulls_Bank.exceptions;

public class SameAccountTransfer extends RuntimeException {
    public SameAccountTransfer(String message) {
        super(message);
    }
}
