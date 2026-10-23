package com.fulls_bank.Fulls_Bank.exceptions;

import org.springframework.dao.ConcurrencyFailureException;
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

    // Cobre o lock otimista (@Version) e o deadlock do banco entre transferências em sentidos opostos:
    // nos dois casos a transação é desfeita e a operação pode ser repetida
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrencyFailure(ConcurrencyFailureException e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 409;
        String message = "The account was changed by another operation, please try again";
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

    @ExceptionHandler(EmailAlreadyRegistered.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegistered(EmailAlreadyRegistered e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentials.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentials e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 401;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(401).body(errorResponse);
    }

    @ExceptionHandler(InvalidPeriod.class)
    public ResponseEntity<ErrorResponse> handleInvalidPeriod(InvalidPeriod e) {
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

    @ExceptionHandler(InvalidAmount.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmount e) {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(status, message, timestamp);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(SameAccountTransfer.class)
    public ResponseEntity<ErrorResponse> handleSameAccountTransfer(SameAccountTransfer e) {
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
