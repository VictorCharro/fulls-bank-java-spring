package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.TransactionRequestDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Transaction;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.InsufficientBalance;
import com.fulls_bank.Fulls_Bank.exceptions.InvalidAmount;
import com.fulls_bank.Fulls_Bank.exceptions.SameAccountTransfer;
import com.fulls_bank.Fulls_Bank.exceptions.TransactionNotFound;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Long SENDER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account sender;
    private Account receiver;

    @BeforeEach
    void setUp() {
        sender = new Account();
        sender.setId(SENDER_ID);
        sender.setBalance(new BigDecimal("50"));

        receiver = new Account();
        receiver.setId(RECEIVER_ID);
        receiver.setBalance(new BigDecimal("20"));
    }

    private TransactionRequestDTO request(TransactionType type, Long senderId, Long receiverId, String amount) {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setType(type);
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);
        dto.setAmount(amount == null ? null : new BigDecimal(amount));
        return dto;
    }

    @Test
    void deposit_shouldIncreaseReceiverBalance() {
        when(accountRepository.findById(RECEIVER_ID)).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(
                request(TransactionType.DEPOSIT, null, RECEIVER_ID, "100"));

        assertEquals(new BigDecimal("120"), receiver.getBalance());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(receiver, result.getReceiver());
        verify(accountRepository).save(receiver);
    }

    @Test
    void withdrawal_shouldDecreaseSenderBalance() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(
                request(TransactionType.WITHDRAWAL, SENDER_ID, null, "30"));

        assertEquals(new BigDecimal("20"), sender.getBalance());
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(sender, result.getSender());
        verify(accountRepository).save(sender);
    }

    @Test
    void withdrawal_withInsufficientBalance_shouldThrowAndNotSave() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));

        assertThrows(InsufficientBalance.class, () -> transactionService.createTransaction(
                request(TransactionType.WITHDRAWAL, SENDER_ID, null, "100")));

        assertEquals(new BigDecimal("50"), sender.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldMoveBalanceBetweenAccounts() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(RECEIVER_ID)).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(
                request(TransactionType.TRANSFER, SENDER_ID, RECEIVER_ID, "30"));

        assertEquals(new BigDecimal("20"), sender.getBalance());
        assertEquals(new BigDecimal("50"), receiver.getBalance());
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
    }

    @Test
    void transfer_withInsufficientBalance_shouldThrowAndKeepBalances() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(RECEIVER_ID)).thenReturn(Optional.of(receiver));

        assertThrows(InsufficientBalance.class, () -> transactionService.createTransaction(
                request(TransactionType.TRANSFER, SENDER_ID, RECEIVER_ID, "100")));

        assertEquals(new BigDecimal("50"), sender.getBalance());
        assertEquals(new BigDecimal("20"), receiver.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_toSameAccount_shouldThrowSameAccountTransfer() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));

        assertThrows(SameAccountTransfer.class, () -> transactionService.createTransaction(
                request(TransactionType.TRANSFER, SENDER_ID, SENDER_ID, "10")));

        assertEquals(new BigDecimal("50"), sender.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_withZeroAmount_shouldThrowInvalidAmount() {
        assertThrows(InvalidAmount.class, () -> transactionService.createTransaction(
                request(TransactionType.DEPOSIT, null, RECEIVER_ID, "0")));

        verify(accountRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_withNegativeAmount_shouldThrowInvalidAmount() {
        assertThrows(InvalidAmount.class, () -> transactionService.createTransaction(
                request(TransactionType.DEPOSIT, null, RECEIVER_ID, "-10")));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_withNullAmount_shouldThrowInvalidAmount() {
        assertThrows(InvalidAmount.class, () -> transactionService.createTransaction(
                request(TransactionType.DEPOSIT, null, RECEIVER_ID, null)));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deposit_withUnknownAccount_shouldThrowAccountNotFound() {
        when(accountRepository.findById(RECEIVER_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> transactionService.createTransaction(
                request(TransactionType.DEPOSIT, null, RECEIVER_ID, "10")));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdrawal_withUnknownAccount_shouldThrowAccountNotFound() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> transactionService.createTransaction(
                request(TransactionType.WITHDRAWAL, SENDER_ID, null, "10")));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_withUnknownReceiver_shouldThrowAccountNotFound() {
        when(accountRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(RECEIVER_ID)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> transactionService.createTransaction(
                request(TransactionType.TRANSFER, SENDER_ID, RECEIVER_ID, "10")));

        assertEquals(new BigDecimal("50"), sender.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void findTransactionById_withExistingId_shouldReturnTransaction() {
        Transaction transaction = new Transaction();
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        assertSame(transaction, transactionService.findTransactionById(10L));
    }

    @Test
    void findTransactionById_withUnknownId_shouldThrowTransactionNotFound() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFound.class, () -> transactionService.findTransactionById(10L));
    }
}
