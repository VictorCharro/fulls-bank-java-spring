# 08 — Testes

Código: `TEST/*`. Ferramentas: JUnit 5 e Mockito (`spring-boot-starter-test`).

## Estado atual

| Teste | Tipo | Observação |
|---|---|---|
| `AbstractIntegrationTest` | Base de integração | Contexto completo + MockMvc + PostgreSQL 16 em container (Testcontainers, precisa de Docker), compartilhado entre as classes; limpa as tabelas antes de cada teste |
| `FullsBankApplicationTests` | Contexto Spring | Valida as migrations do Flyway contra o esquema das entidades |
| `controllers/BankFlowIntegrationTest` | Integração (issue #12) | Fluxo criar cliente → criar conta → depositar → sacar → transferir, conferindo saldos e transações; bloqueio (409) ao excluir conta e cliente em uso |
| `controllers/ErrorResponsesIntegrationTest` | Integração (issue #12) | Status HTTP de erro com banco real: saldo insuficiente, valor zero, mesma conta, conta inexistente, CPF duplicado ou inválido, cliente, conta e transação inexistentes |
| `controllers/AccountControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issues #25, #27, #28 e #29; `GET /account/number/{number}`, 404 por id, 409 ao excluir conta com transações e 503 sem número de conta livre |
| `controllers/ClientControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issue #28; 409 ao excluir cliente com contas |
| `controllers/TransactionControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issues #30, #3 e #4; validação de `amount`, 400 do service e `GET /transaction/{id}` (200 e 404) |
| `services/TransactionServiceTest` | Unitário (Mockito) | Issue #1 |
| `services/AccountServiceTest` | Unitário (Mockito) | Issue #2 |
| `services/ClientServiceTest` | Unitário (Mockito) | Issue #2 |

Ainda sem testes: concorrência (issue #14).

## Convenções

- Pacote de teste espelha o pacote do código testado.
- Nome da classe: `<Classe>Test`.
- Nome do método: `<acao>_<condicao>_<resultado>`, por exemplo `withdrawal_withInsufficientBalance_shouldThrowAndNotSave`.
- Integração: estender `AbstractIntegrationTest` (contexto, banco em container e limpeza das tabelas já prontos) e chamar a API via `MockMvc`.
- Unitários: `@ExtendWith(MockitoExtension.class)`, `@Mock` nos repositórios, `@InjectMocks` no service. Sem subir o Spring.
- Para erros, conferir o tipo da exceção **e** que nada foi persistido (`verify(..., never())`).
- Dinheiro sempre com `BigDecimal` criado por `new BigDecimal("...")` (string), nunca `double`.
- Cada regra de [05](05-regras-de-negocio.md) deve ter ao menos um teste de sucesso e um de falha.

## Como rodar

```bash
./mvnw test                                   # todos
./mvnw test -Dtest=TransactionServiceTest     # uma classe
```

## Planejado

- Teste de concorrência para saque e transferência (#14).
- Testes de autenticação com Spring Security (#15).
- Execução automática no CI (#20).
