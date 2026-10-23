package com.fulls_bank.Fulls_Bank;

import com.fulls_bank.Fulls_Bank.repositories.AccountRepository;
import com.fulls_bank.Fulls_Bank.repositories.AppUserRepository;
import com.fulls_bank.Fulls_Bank.repositories.ClientRepository;
import com.fulls_bank.Fulls_Bank.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base dos testes que sobem o contexto completo: PostgreSQL real em container,
 * migrations do Flyway e a pilha HTTP inteira via MockMvc. O container sobe uma vez por
 * execução e é compartilhado com o contexto do Spring (que fica em cache entre as classes);
 * ele é encerrado pelo Ryuk do Testcontainers ao fim da JVM. Cada teste começa com as tabelas vazias.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected ClientRepository clientRepository;

    @Autowired
    protected AppUserRepository appUserRepository;

    @BeforeEach
    void cleanDatabase() {
        appUserRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();
    }
}
