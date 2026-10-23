# 03 — Domínio

Código: `MAIN/entities`, `MAIN/enums`, `MAIN/dto`.

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
| `cpf` | String | único, exatamente 11 caracteres (`@Size`) |
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
| `description` | String | não utilizado hoje |
| `type` | TransactionType | enum salvo como texto |
| `sender` | Account | `@ManyToOne`, coluna `sender_id`, nulo em depósito |
| `receiver` | Account | `@ManyToOne`, coluna `receiver_id`, nulo em saque |

## Enums

- `AccountType`: `CHECKING` (corrente), `SAVINGS` (poupança), `SALARY` (salário).
- `TransactionType`: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`.
- `Role`: `USER`, `ADMIN`.

## DTOs

| DTO | Campos | Validações |
|---|---|---|
| `AccountRequestDTO` | `type`, `clientId` | ambos `@NotNull` |
| `AccountResponseDTO` | `accountNumber`, `type`, `balance`, `sentTransactions`, `receivedTransactions` | — |
| `TransactionRequestDTO` | `senderId`, `receiverId`, `amount`, `type` | `senderId`/`receiverId` `@NotNull` (padrão `0L`), `amount` `@Positive`, `type` `@NotNull` |
| `RegisterRequestDTO` | `email`, `password` | `email` `@NotBlank` `@Email`, até 255 caracteres; `password` `@NotBlank`, mínimo de 8 caracteres e máximo de 72 **bytes** em UTF-8 (limite do BCrypt) |
| `LoginRequestDTO` | `email`, `password` | ambos `@NotBlank` |
| `TokenResponseDTO` | `accessToken`, `tokenType`, `expiresIn` | — |
| `UserResponseDTO` | `id`, `email`, `role` | — |
| `TransactionResponseDTO` | `id`, `amount`, `timestamp`, `type`, `senderId`, `receiverId` | — |

`Client` ainda é usado diretamente como corpo e resposta nos endpoints de cliente (sem DTO).
