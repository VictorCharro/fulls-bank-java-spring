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
| `type` | AccountType | enum salvo como texto |
| `client` | Client | `@ManyToOne`, `@NotNull`, coluna `client_id` |
| `sentTransactions` | List\<Transaction\> | `@OneToMany(mappedBy = "sender")` |
| `receivedTransactions` | List\<Transaction\> | `@OneToMany(mappedBy = "receiver")` |

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

## DTOs

| DTO | Campos | Validações |
|---|---|---|
| `AccountRequestDTO` | `type`, `clientId` | ambos `@NotNull` |
| `AccountResponseDTO` | `accountNumber`, `type`, `balance`, `sentTransactions`, `receivedTransactions` | — |
| `TransactionRequestDTO` | `senderId`, `receiverId`, `amount`, `type` | `senderId`/`receiverId` `@NotNull` (padrão `0L`), `amount` `@Positive`, `type` `@NotNull` |
| `TransactionResponseDTO` | `id`, `amount`, `timestamp`, `type`, `senderId`, `receiverId` | — |

`Client` ainda é usado diretamente como corpo e resposta nos endpoints de cliente (sem DTO).
