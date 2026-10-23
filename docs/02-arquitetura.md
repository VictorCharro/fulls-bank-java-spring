# 02 — Arquitetura

Arquitetura em camadas, com pacote raiz `com.fulls_bank.Fulls_Bank`.

## Pacotes

| Pacote | Responsabilidade |
|---|---|
| `controllers` | Endpoints REST. Recebem a requisição, validam com `@Valid` e delegam ao service. |
| `services` | Regras de negócio e transações de banco (`@Transactional`). |
| `repositories` | Interfaces Spring Data JPA. |
| `entities` | Entidades JPA: `Client`, `Account`, `Transaction`. |
| `dto` | Objetos de entrada e saída da API. |
| `enums` | `AccountType`, `TransactionType`. |
| `exceptions` | Exceções de negócio, `ErrorResponse` e `GlobalExceptionHandler`. |

## Fluxo de uma requisição

```
HTTP → Controller (@Valid) → Service (regra + @Transactional) → Repository → PostgreSQL
                                   │
              exceção de negócio ──┴──► GlobalExceptionHandler → ErrorResponse (JSON)
```

## Dependências entre camadas

- Controller depende só de Service.
- `AccountService` usa `AccountRepository` e `ClientRepository`.
- `TransactionService` usa `TransactionRepository` e `AccountRepository`.
- Injeção por construtor com `@RequiredArgsConstructor` (Lombok).

## Decisões

| Decisão | Motivo |
|---|---|
| `BigDecimal` para valores monetários | Evita erros de ponto flutuante. |
| Exceções de negócio próprias + handler global | Respostas de erro consistentes, controllers sem `try/catch`. |
| DTOs de entrada e saída | Não expor entidades JPA. Ainda não aplicado em todos os endpoints (ver [09](09-problemas-conhecidos.md)). |
| Transação registrada como entidade própria | Histórico auditável de movimentações. |

## Evoluções planejadas

Spring Security com JWT, Flyway, Testcontainers, Docker, CI e deploy na AWS: ver [roadmap](10-roadmap.md).
