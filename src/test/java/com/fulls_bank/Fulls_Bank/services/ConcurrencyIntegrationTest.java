package com.fulls_bank.Fulls_Bank.services;

import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import com.fulls_bank.Fulls_Bank.dto.TransactionRequestDTO;
import com.fulls_bank.Fulls_Bank.entities.Account;
import com.fulls_bank.Fulls_Bank.entities.Client;
import com.fulls_bank.Fulls_Bank.enums.AccountType;
import com.fulls_bank.Fulls_Bank.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prova o controle de concorrência de Account (@Version, issue #13) com threads reais e PostgreSQL real.
 * Operações simultâneas na mesma conta podem perder a disputa (e são desfeitas), mas
 * nunca se sobrescrevem: o saldo final sempre bate com as operações que deram certo.
 */
class ConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final int THREADS = 10;

    @Autowired
    private TransactionService transactionService;

    @Test
    void concurrentWithdrawals_fromTheSameAccount_shouldNeverLoseAnUpdate() throws Exception {
        Client client = saveClient();
        Account account = saveAccount(client, 1001, "1000.00");
        BigDecimal amount = new BigDecimal("100.00");

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            tasks.add(() -> tryTransaction(TransactionType.WITHDRAWAL, account.getId(), null, amount));
        }
        int succeeded = countSuccesses(runTogether(tasks));

        BigDecimal expected = new BigDecimal("1000.00").subtract(amount.multiply(BigDecimal.valueOf(succeeded)));
        assertTrue(succeeded >= 1, "pelo menos um saque deve vencer a disputa");
        assertEquals(0, expected.compareTo(balanceOf(account.getId())),
                "saldo final deve refletir exatamente os saques que deram certo");
        assertEquals(succeeded, transactionRepository.count(),
                "só os saques que deram certo geram transação");
    }

    @Test
    void concurrentTransfers_inBothDirections_shouldKeepTheTotalAndMatchTheSuccessfulOnes() throws Exception {
        Client client = saveClient();
        Account accountA = saveAccount(client, 1001, "500.00");
        Account accountB = saveAccount(client, 1002, "500.00");
        BigDecimal fromA = new BigDecimal("50.00");
        BigDecimal fromB = new BigDecimal("20.00");

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < THREADS / 2; i++) {
            tasks.add(() -> tryTransaction(TransactionType.TRANSFER, accountA.getId(), accountB.getId(), fromA));
            tasks.add(() -> tryTransaction(TransactionType.TRANSFER, accountB.getId(), accountA.getId(), fromB));
        }
        List<Boolean> results = runTogether(tasks);

        int succeededFromA = 0;
        int succeededFromB = 0;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i)) {
                if (i % 2 == 0) {
                    succeededFromA++;
                } else {
                    succeededFromB++;
                }
            }
        }
        BigDecimal movedFromA = fromA.multiply(BigDecimal.valueOf(succeededFromA));
        BigDecimal movedFromB = fromB.multiply(BigDecimal.valueOf(succeededFromB));
        BigDecimal balanceA = balanceOf(accountA.getId());
        BigDecimal balanceB = balanceOf(accountB.getId());

        assertTrue(succeededFromA + succeededFromB >= 1, "pelo menos uma transferência deve vencer a disputa");
        assertEquals(0, new BigDecimal("1000.00").compareTo(balanceA.add(balanceB)),
                "o total entre as duas contas não pode mudar");
        assertEquals(0, new BigDecimal("500.00").subtract(movedFromA).add(movedFromB).compareTo(balanceA));
        assertEquals(0, new BigDecimal("500.00").add(movedFromA).subtract(movedFromB).compareTo(balanceB));
        assertEquals(succeededFromA + succeededFromB, transactionRepository.count());
    }

    /**
     * Devolve true se a operação foi gravada e false se perdeu a disputa (lock otimista ou deadlock do banco);
     * nos dois casos a transação foi desfeita e a API responderia 409.
     */
    private boolean tryTransaction(TransactionType type, Long senderId, Long receiverId, BigDecimal amount) {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setType(type);
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);
        dto.setAmount(amount);
        try {
            transactionService.createTransaction(dto);
            return true;
        } catch (ConcurrencyFailureException e) {
            return false;
        }
    }

    /** Solta todas as tarefas ao mesmo tempo para maximizar a disputa; qualquer outro erro falha o teste. */
    private List<Boolean> runTogether(List<Callable<Boolean>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Callable<Boolean> task : tasks) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return task.call();
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "todas as tarefas devem estar prontas antes do início");
            start.countDown();

            List<Boolean> results = new ArrayList<>();
            for (Future<Boolean> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private int countSuccesses(List<Boolean> results) {
        return (int) results.stream().filter(Boolean::booleanValue).count();
    }

    private Client saveClient() {
        Client client = new Client();
        client.setCpf("12345678901");
        client.setName("Ana");
        client.setLastName("Silva");
        return clientRepository.save(client);
    }

    private Account saveAccount(Client client, int number, String balance) {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setBalance(new BigDecimal(balance));
        account.setType(AccountType.CHECKING);
        account.setClient(client);
        return accountRepository.save(account);
    }

    private BigDecimal balanceOf(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow().getBalance();
    }
}
