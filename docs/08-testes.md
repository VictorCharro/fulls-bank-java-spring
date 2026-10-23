# 08 — Testes

Código: `TEST/*`. Ferramentas: JUnit 5 e Mockito (`spring-boot-starter-test`).

## Estado atual

| Teste | Tipo | Observação |
|---|---|---|
| `FullsBankApplicationTests` | Contexto Spring | Exige PostgreSQL local (issue #11) |
| `controllers/AccountControllerTest` | Web (`@WebMvcTest`, MockMvc) | Issue #25; só `GET /account/number/{number}` |
| `services/TransactionServiceTest` | Unitário (Mockito) | Issue #1 |
| `services/AccountServiceTest` | Unitário (Mockito) | Issue #2 |
| `services/ClientServiceTest` | Unitário (Mockito) | Issue #2 |

Ainda sem testes: controllers (issue #12), concorrência (issue #14).

## Convenções

- Pacote de teste espelha o pacote do código testado.
- Nome da classe: `<Classe>Test`.
- Nome do método: `<acao>_<condicao>_<resultado>`, por exemplo `withdrawal_withInsufficientBalance_shouldThrowAndNotSave`.
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

- Testes de integração com Testcontainers e PostgreSQL (#11, #12).
- Teste de concorrência para saque e transferência (#14).
- Testes de autenticação com Spring Security (#15).
- Execução automática no CI (#20).
