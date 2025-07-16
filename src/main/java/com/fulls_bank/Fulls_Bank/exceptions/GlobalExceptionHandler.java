package com.fulls_bank.Fulls_Bank.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFound.class)
    public ResponseEntity<ErrorResponse> handleClientNotFound(ClientNotFound e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(ExistingAccount.class)
    public ResponseEntity<ErrorResponse> handleExistingAccount(ExistingAccount e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(AccountNotFound.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFound e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(InsufficientBalance.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalance e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(NegativeAmount.class)
    public ResponseEntity<ErrorResponse> handleNegativeAmount(NegativeAmount e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<String> errors = new ArrayList<>();

        for (FieldError fieldError : fieldErrors) {
            String errorMessage = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            errors.add(errorMessage);
        }
        String message = String.join(", ",errors);
        ErrorResponse errorResponse = new ErrorResponse(400, message, LocalDateTime.now());
        return ResponseEntity.status(400).body(errorResponse);
    }
}
