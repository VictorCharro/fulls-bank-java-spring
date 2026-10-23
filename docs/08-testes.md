# 08 — Testes

Código: `TEST/*`. Ferramentas: JUnit 5 e Mockito (`spring-boot-starter-test`).

## Estado atual

| Teste | Tipo | Observação |
|---|---|---|
| `AbstractIntegrationTest` | Base de integração | Contexto completo + MockMvc + PostgreSQL 16 em container (Testcontainers, precisa de Docker), compartilhado entre as classes; limpa as tabelas antes de cada teste |
| `FullsBankApplicationTests` | Contexto Spring | Valida as migrations do Flyway contra o esquema das entidades |
| `controllers/AuthControllerTest` | Web (`@WebMvcTest`) | Issue #15; cadastro (201, 400 por validação e por e-mail duplicado) e login (token, 401, 400) |
| `services/AuthServiceTest` | Unitário (Mockito) | Issue #15; papel `USER`, hash da senha, e-mail normalizado, mesma resposta para e-mail desconhecido e senha errada |
| `security/AuthIntegrationTest` | Integração | Issue #15; cadastro e login reais, token abre os endpoints, `USER` recebe 403 em `DELETE` e listagens globais, `ADMIN` passa, token sem formato, expirado ou com outra chave recebe 401 |
| `security/AdminBootstrapTest`, `security/SecurityConfigTest` | Unitário | Criação do primeiro administrador (e senha acima de 72 bytes); falha imediata sem `JWT_SECRET` válido |
| `security/ErrorDispatchIntegrationTest` | Integração (servidor real) | Erros do Spring MVC (JSON malformado, 415) mantêm o status real em vez de virar 401; `/error` direto segue protegido |
| `controllers/BankFlowIntegrationTest` | Integração (issue #12) | Fluxo criar cliente → criar conta → depositar → sacar → transferir, conferindo saldos e transações; bloqueio (409) ao excluir conta e cliente em uso |
| `services/ConcurrencyIntegrationTest` | Integração (issue #14) | Threads reais na mesma conta: saques simultâneos e transferências em sentidos opostos; o saldo final bate com as operações que deram certo e o total entre as contas não muda |
| `controllers/ErrorResponsesIntegrationTest` | Integração (issue #12) | Status HTTP de erro com banco real: saldo insuficiente, valor zero, mesma conta, conta inexistente, CPF duplicado ou inválido, cliente, conta e transação inexistentes |
| `controllers/AccountControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issues #25, #27, #28 e #29; `GET /account/number/{number}`, 404 por id, 409 ao excluir conta com transações e 503 sem número de conta livre |
| `controllers/ClientControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issue #28; 409 ao excluir cliente com contas |
| `controllers/TransactionControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issues #30, #3 e #4; validação de `amount`, 400 do service e `GET /transaction/{id}` (200 e 404) |
| `services/TransactionServiceTest` | Unitário (Mockito) | Issue #1 |
| `services/AccountServiceTest` | Unitário (Mockito) | Issue #2 |
| `services/ClientServiceTest` | Unitário (Mockito) | Issue #2 |

## Convenções

- Pacote de teste espelha o pacote do código testado.
- Nome da classe: `<Classe>Test`.
- Nome do método: `<acao>_<condicao>_<resultado>`, por exemplo `withdrawal_withInsufficientBalance_shouldThrowAndNotSave`.
- `@WebMvcTest` de controller: usar `@ApiSecurityTest` (regras de segurança reais e segredo de teste) e `@WithMockUser(roles = "ADMIN")`; para testar o 401 ou 403, trocar o usuário no teste.
- Integração: estender `AbstractIntegrationTest` (contexto, banco em container e limpeza das tabelas já prontos) e chamar a API via `MockMvc`.
- Unitários: `@ExtendWith(MockitoExtension.class)`, `@Mock` nos repositórios, `@InjectMocks` no service. Sem subir o Spring.
- Para erros, conferir o tipo da exceção **e** que nada foi persistido (`verify(..., never())`).
- Em testes de integração (banco real), não há mocks: para erros, conferir o status HTTP **e** o estado do banco depois (saldo, `repository.count()`). `verify` fica só nos unitários.
- Dinheiro sempre com `BigDecimal` criado por `new BigDecimal("...")` (string), nunca `double`.
- Cada regra de [05](05-regras-de-negocio.md) deve ter ao menos um teste de sucesso e um de falha.

## Como rodar

```bash
./mvnw test                                   # todos
./mvnw test -Dtest=TransactionServiceTest     # uma classe
```

## Planejado

