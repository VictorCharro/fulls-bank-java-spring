# 03 — Domínio

Código: `MAIN/entities`, `MAIN/enums`, `MAIN/dto`, `MAIN/mappers`.

## Modelo

```
Client 1 ──── N Account 1 ──── N Transaction (como sender)
                      1 ──── N Transaction (como receiver)
```

## Entidades

### Client (`entities/Client.java`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | Long | PK, identity |
| `cpf` | String | único, exatamente 11 caracteres (`@Size`; o DTO de entrada exige 11 dígitos) |
| `name` | String | `@NotBlank` |
| `lastName` | String | `@NotBlank` |

### Account (`entities/Account.java`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | Long | PK, identity |
| `accountNumber` | int | único |
| `balance` | BigDecimal | `@NotNull` |
| `version` | Long | `@Version` (lock otimista), coluna `version` |
| `type` | AccountType | enum salvo como texto |
| `client` | Client | `@ManyToOne`, `@NotNull`, coluna `client_id` |
| `sentTransactions` | List\<Transaction\> | `@OneToMany(mappedBy = "sender")` |
| `receivedTransactions` | List\<Transaction\> | `@OneToMany(mappedBy = "receiver")` |

### AppUser (`entities/AppUser.java`, tabela `app_user`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | Long | PK, identity |
| `email` | String | único, não nulo |
| `passwordHash` | String | hash BCrypt, não nulo |
| `role` | Role | enum salvo como texto |

### Transaction (`entities/Transaction.java`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | Long | PK, identity |
| `amount` | BigDecimal | `@Positive` |
| `timestamp` | LocalDateTime | preenchido no service |
| `type` | TransactionType | enum salvo como texto |
| `sender` | Account | `@ManyToOne`, coluna `sender_id`, nulo em depósito |
| `receiver` | Account | `@ManyToOne`, coluna `receiver_id`, nulo em saque |

## Enums

- `AccountType`: `CHECKING` (corrente), `SAVINGS` (poupança), `SALARY` (salário).
- `TransactionType`: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`.
- `Role`: `USER`, `ADMIN`.

## DTOs

A conversão entidade → DTO de resposta é feita por MapStruct (`mappers/AccountMapper`, `mappers/ClientMapper` e `mappers/TransactionMapper`, ver [02](02-arquitetura.md)). `AccountResponseDTO` não preenche `sentTransactions` e `receivedTransactions` (saem `null`): o histórico vem do extrato (`GET /account/{id}/statement`). Em `TransactionResponseDTO`, `senderId` e `receiverId` vêm de `sender.id` e `receiver.id` e ficam `null` quando a conta não existe (depósito não tem origem, saque não tem destino).

| DTO | Campos | Validações |
|---|---|---|
| `AccountRequestDTO` | `type`, `clientId` | ambos `@NotNull` |
| `ClientRequestDTO` | `cpf`, `name`, `lastName` | `cpf` `@NotBlank` e exatamente 11 dígitos (`@Pattern`); `name` e `lastName` `@NotBlank` |
| `ClientUpdateDTO` | `name`, `lastName` | ambos `@NotBlank` (o CPF não faz parte do `PUT`) |
| `ClientResponseDTO` | `id`, `cpf`, `name`, `lastName` | — |
| `AccountResponseDTO` | `accountNumber`, `type`, `balance`, `sentTransactions`, `receivedTransactions` | — |
| `TransactionRequestDTO` | `senderId`, `receiverId`, `amount`, `type` | `type` `@NotNull`; `amount` `@NotNull` `@Positive`; `senderId`/`receiverId` sem valor padrão, exigidos conforme o tipo por `@ValidTransactionRequest` (depósito: `receiverId`; saque: `senderId`; transferência: ambos) |
| `RegisterRequestDTO` | `email`, `password` | `email` `@NotBlank` `@Email`, até 255 caracteres; `password` `@NotBlank`, mínimo de 8 caracteres e máximo de 72 **bytes** em UTF-8 (limite do BCrypt) |
| `LoginRequestDTO` | `email`, `password` | ambos `@NotBlank` |
| `TokenResponseDTO` | `accessToken`, `tokenType`, `expiresIn` | — |
| `UserResponseDTO` | `id`, `email`, `role` | — |
| `TransactionResponseDTO` | `id`, `amount`, `timestamp`, `type`, `senderId`, `receiverId` | — |

Os endpoints de cliente usam só DTOs; o `id` enviado no corpo é ignorado.
