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
| `mappers` | Interfaces MapStruct que convertem entidade em DTO de resposta (`AccountMapper`, `TransactionMapper`). |
| `enums` | `AccountType`, `TransactionType`. |
| `exceptions` | Exceções de negócio, `ErrorResponse` e `GlobalExceptionHandler`. |

## Fluxo de uma requisição

```
HTTP → Controller (@Valid) → Service (regra + @Transactional) → Repository → PostgreSQL
                                   │
              exceção de negócio ──┴──► GlobalExceptionHandler → ErrorResponse (JSON)
```

## Dependências entre camadas

- Controller depende de Service e dos mappers: a conversão entidade → DTO fica nos mappers, não em métodos dos services. Um service só usa mapper quando ele mesmo devolve DTO (hoje, `AccountService.createAccount`, que usa `AccountMapper`).
- `AccountService` usa `AccountRepository` e `ClientRepository`.
- `TransactionService` usa `TransactionRepository` e `AccountRepository`.
- Injeção por construtor com `@RequiredArgsConstructor` (Lombok).

## Decisões

| Decisão | Motivo |
|---|---|
| `BigDecimal` para valores monetários | Evita erros de ponto flutuante. |
| Exceções de negócio próprias + handler global | Respostas de erro consistentes, controllers sem `try/catch`. |
| MapStruct para entidade → DTO | Mapeamento gerado em compilação (sem reflexão) e conferido: com `unmappedTargetPolicy = ERROR`, um campo novo no DTO sem mapeamento quebra a compilação. |
| DTOs de entrada e saída | Não expor entidades JPA. Ainda não aplicado em todos os endpoints (ver [09](09-problemas-conhecidos.md)). |
| Transação registrada como entidade própria | Histórico auditável de movimentações. |

## Evoluções planejadas

Deploy na AWS: ver [roadmap](10-roadmap.md). Docker já está disponível: ver [configuração](07-configuracao-e-execucao.md).
