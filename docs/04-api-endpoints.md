# 04 — API

Código: `MAIN/controllers`. Formato de erro: [06](06-erros-e-excecoes.md). Itens marcados com ⚠️ têm problema descrito em [09](09-problemas-conhecidos.md).

## Clientes — `/client` (`ClientController`)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/client` | `Client` (`cpf`, `name`, `lastName`) | 200, `Client` ⚠️ |
| GET | `/client` | — | 200, lista de `Client` ⚠️ |
| GET | `/client/{id}` | — | 200, `Client` ⚠️ |
| PUT | `/client/{id}` | `Client` (só `name` e `lastName` são atualizados) | 200, `Client` ⚠️ |
| DELETE | `/client/{id}` | — | 200, sem corpo; 409 se o cliente tiver contas |

## Contas — `/account` (`AccountController`)

| Método | Rota | Entrada | Resposta |
|---|---|---|---|
| POST | `/account` | `AccountRequestDTO` | 201, `AccountResponseDTO` |
| GET | `/account/{id}` | — | 200, `AccountResponseDTO` (listas de transações vêm nulas) ⚠️ |
| GET | `/account/number/{number}` | — | 200, `AccountResponseDTO`; 404 se a conta não existir |
| DELETE | `/account/{id}` | — | 200, sem corpo ⚠️; 409 se a conta tiver transações |

Depósito, saque e transferência são feitos somente por `POST /transaction`.

## Transações — `/transaction` (`TransactionController`)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/transaction` | `TransactionRequestDTO` | 201, `TransactionResponseDTO` |
| GET | `/transaction` | — | 200, lista de `TransactionResponseDTO` |
| GET | `/transaction/{id}` | — | 200, `Transaction` (entidade) se existir; 200 com corpo `null` se não existir ⚠️ |

### Exemplos de `POST /transaction`

Depósito (usa só `receiverId`):

```json
{ "type": "DEPOSIT", "receiverId": 1, "amount": 100.00 }
```

Saque (usa só `senderId`):

```json
{ "type": "WITHDRAWAL", "senderId": 1, "amount": 30.00 }
```

Transferência (usa os dois):

```json
{ "type": "TRANSFER", "senderId": 1, "receiverId": 2, "amount": 50.00 }
```

Regras: [05](05-regras-de-negocio.md).
