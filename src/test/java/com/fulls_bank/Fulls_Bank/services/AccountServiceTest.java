package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.AccountRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.InsufficientBalance;
import com.fulls_bank.Fulls_Bank.exceptions.NegativeAmount;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long CLIENT_ID = 10L;
    private static final int ACCOUNT_NUMBER = 1234;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(ACCOUNT_ID);
        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("100"));
    }

    // generateUniqueAccountNumber

    @Test
    void generateUniqueAccountNumber_shouldReturnNumberBetween1000And9999() {
        when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(false);

        Integer number = accountService.generateUniqueAccountNumber();

        assertTrue(number >= 1000 && number <= 9999);
    }

    @Test
    void generateUniqueAccountNumber_whenNumberIsTaken_shouldRetryUntilFree() {
        when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(true, true, false);

        Integer number = accountService.generateUniqueAccountNumber();

        assertTrue(number >= 1000 && number <= 9999);
        verify(accountRepository, times(3)).existsByAccountNumber(anyInt());
    }

    // buscas

    @Test
    void findAccountByAccountNumber_withExistingNumber_shouldReturnAccount() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertSame(account, accountService.findAccountByAccountNumber(ACCOUNT_NUMBER));
    }

    @Test
    void findAccountByAccountNumber_withUnknownNumber_shouldThrowAccountNotFound() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> accountService.findAccountByAccountNumber(ACCOUNT_NUMBER));
    }

    @Test
    void findAccountById_withExistingId_shouldReturnAccount() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertSame(account, accountService.findAccountById(ACCOUNT_ID));
    }

    @Test
    void findAccountById_withUnknownId_shouldThrowIdNotFound() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(IdNotFound.class, () -> accountService.findAccountById(ACCOUNT_ID));
    }

    // deposit

    @Test
    void deposit_withValidAmount_shouldIncreaseBalanceAndSave() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        accountService.deposit(ACCOUNT_NUMBER, new BigDecimal("25"));

        assertEquals(new BigDecimal("125"), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void deposit_withNegativeAmount_shouldThrowAndNotSave() {
        assertThrows(NegativeAmount.class, () -> accountService.deposit(ACCOUNT_NUMBER, new BigDecimal("-1")));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_withUnknownAccount_shouldThrowAndNotSave() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> accountService.deposit(ACCOUNT_NUMBER, new BigDecimal("10")));

        verify(accountRepository, never()).save(any());
    }

    // withdrawal

    @Test
    void withdrawal_withEnoughBalance_shouldDecreaseBalanceAndSave() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        accountService.withdrawal(ACCOUNT_NUMBER, new BigDecimal("40"));

        assertEquals(new BigDecimal("60"), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawal_withExactBalance_shouldLeaveBalanceZero() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        accountService.withdrawal(ACCOUNT_NUMBER, new BigDecimal("100"));

        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawal_withInsufficientBalance_shouldThrowAndNotSave() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalance.class,
                () -> accountService.withdrawal(ACCOUNT_NUMBER, new BigDecimal("100.01")));

        assertEquals(new BigDecimal("100"), account.getBalance());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdrawal_withNegativeAmount_shouldThrowAndNotSave() {
        assertThrows(NegativeAmount.class, () -> accountService.withdrawal(ACCOUNT_NUMBER, new BigDecimal("-1")));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdrawal_withUnknownAccount_shouldThrowAndNotSave() {
        when(accountRepository.findAccountByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> accountService.withdrawal(ACCOUNT_NUMBER, new BigDecimal("10")));

        verify(accountRepository, never()).save(any());
    }

    // deleteAccount

    @Test
    void deleteAccount_withExistingId_shouldDelete() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        accountService.deleteAccount(ACCOUNT_ID);

        verify(accountRepository).deleteById(ACCOUNT_ID);
    }

    @Test
    void deleteAccount_withUnknownId_shouldThrowAndNotDelete() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(IdNotFound.class, () -> accountService.deleteAccount(ACCOUNT_ID));

        verify(accountRepository, never()).deleteById(any());
    }

    // createAccount

    @Test
    void createAccount_withExistingClient_shouldSaveWithZeroBalanceAndGeneratedNumber() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountRequestDTO request = new AccountRequestDTO();
        request.setType(AccountType.SAVINGS);
        request.setClientId(CLIENT_ID);

        AccountResponseDTO response = accountService.createAccount(request);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertSame(client, saved.getClient());
        assertEquals(AccountType.SAVINGS, saved.getType());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getBalance()));
        assertTrue(saved.getAccountNumber() >= 1000 && saved.getAccountNumber() <= 9999);
        assertEquals(saved.getAccountNumber(), response.getAccountNumber());
        assertEquals(AccountType.SAVINGS, response.getType());
    }

    @Test
    void createAccount_withUnknownClient_shouldThrowAndNotSave() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        AccountRequestDTO request = new AccountRequestDTO();
        request.setType(AccountType.CHECKING);
        request.setClientId(CLIENT_ID);

        assertThrows(ClientNotFound.class, () -> accountService.createAccount(request));

        verify(accountRepository, never()).save(any());
    }
}
