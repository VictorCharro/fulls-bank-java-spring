package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.dto.AccountRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.AccountResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.AccountNumberUnavailable;
import com.fulls_bank.Fulls_Bank.exceptions.ClientNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.IdNotFound;
import com.fulls_bank.Fulls_Bank.exceptions.ResourceInUse;
import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import com.fulls_bank.Fulls_Bank.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.doThrow;
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

    @Mock
    private TransactionRepository transactionRepository;

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
    void generateUniqueAccountNumber_shouldReturnSixDigitNumber() {
        when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(false);

        Integer number = accountService.generateUniqueAccountNumber();

        assertTrue(isInRange(number));
    }

    @Test
    void generateUniqueAccountNumber_whenNumberIsTaken_shouldRetryUntilFree() {
        when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(true, true, false);

        Integer number = accountService.generateUniqueAccountNumber();

        assertTrue(isInRange(number));
        verify(accountRepository, times(3)).existsByAccountNumber(anyInt());
    }

    @Test
    void generateUniqueAccountNumber_whenAllAttemptsAreTaken_shouldThrowAfterMaxAttempts() {
        when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(true);

        assertThrows(AccountNumberUnavailable.class, () -> accountService.generateUniqueAccountNumber());

        verify(accountRepository, times(AccountService.MAX_NUMBER_ATTEMPTS)).existsByAccountNumber(anyInt());
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

    // deleteAccount

    @Test
    void deleteAccount_withExistingId_shouldDelete() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.existsBySenderIdOrReceiverId(ACCOUNT_ID, ACCOUNT_ID)).thenReturn(false);

        accountService.deleteAccount(ACCOUNT_ID);

        verify(accountRepository).deleteById(ACCOUNT_ID);
    }

    @Test
    void deleteAccount_withTransactions_shouldThrowResourceInUseAndNotDelete() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.existsBySenderIdOrReceiverId(ACCOUNT_ID, ACCOUNT_ID)).thenReturn(true);

        assertThrows(ResourceInUse.class, () -> accountService.deleteAccount(ACCOUNT_ID));

        verify(accountRepository, never()).deleteById(any());
    }

    @Test
    void deleteAccount_whenForeignKeyViolationOnDelete_shouldThrowResourceInUse() {
        when(accountRepository.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.existsBySenderIdOrReceiverId(ACCOUNT_ID, ACCOUNT_ID)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("fk")).when(accountRepository).deleteById(ACCOUNT_ID);

        assertThrows(ResourceInUse.class, () -> accountService.deleteAccount(ACCOUNT_ID));
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
        assertTrue(isInRange(saved.getAccountNumber()));
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

    private static boolean isInRange(int number) {
        return number >= 100_000 && number <= 999_999;
    }
}
