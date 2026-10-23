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

    @ExceptionHandler(IdNotFound.class)
    public ResponseEntity<ErrorResponse> handleIdNotFound(IdNotFound e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(ResourceInUse.class)
    public ResponseEntity<ErrorResponse> handleResourceInUse(ResourceInUse e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 409;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(409).body(errorResponse);
    }

    @ExceptionHandler(TransactionNotFound.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(TransactionNotFound e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(AccountNumberUnavailable.class)
    public ResponseEntity<ErrorResponse> handleAccountNumberUnavailable(AccountNumberUnavailable e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 503;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(503).body(errorResponse);
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
