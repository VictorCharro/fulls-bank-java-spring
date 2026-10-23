# 04 — API

Código: `MAIN/controllers`. Formato de erro: [06](06-erros-e-excecoes.md). Itens marcados com ⚠️ têm problema descrito em [09](09-problemas-conhecidos.md).

A documentação interativa (Swagger UI) está em `/swagger-ui.html` e o OpenAPI em `/v3/api-docs`; veja [07](07-configuracao-e-execucao.md#documentação-interativa-swaggeropenapi).

## Autenticação

Todos os endpoints exigem o cabeçalho `Authorization: Bearer <token>`, exceto `POST /auth/register` e `POST /auth/login`. Sem token (ou com token inválido ou expirado) a resposta é `401`; sem permissão, `403`. Ambas no [formato de erro](06-erros-e-excecoes.md) da API.

Somente `ADMIN`: todo `DELETE` e as listagens globais `GET /client` e `GET /transaction`. O papel `USER` acessa os demais endpoints.

## Autenticação — `/auth` (`AuthController`)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/auth/register` | `RegisterRequestDTO` (`email`, `password` com mínimo de 8 caracteres e máximo de 72 bytes em UTF-8) | 201, `UserResponseDTO` (`id`, `email`, `role`); 400 se o e-mail já existir |
| POST | `/auth/login` | `LoginRequestDTO` (`email`, `password`) | 200, `TokenResponseDTO` (`accessToken`, `tokenType` `Bearer`, `expiresIn` em segundos); 401 se e-mail ou senha estiverem errados |

## Clientes — `/client` (`ClientController`)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/client` | `ClientRequestDTO` (`cpf`, `name`, `lastName`) | 200, `ClientResponseDTO` |
| GET | `/client` | — | 200, lista de `ClientResponseDTO` |
| GET | `/client/{id}` | — | 200, `ClientResponseDTO` |
| PUT | `/client/{id}` | `ClientUpdateDTO` (`name`, `lastName`; o CPF não muda) | 200, `ClientResponseDTO` |
| DELETE | `/client/{id}` | — | 200, sem corpo; 409 se o cliente tiver contas |

## Contas — `/account` (`AccountController`)

| Método | Rota | Entrada | Resposta |
|---|---|---|---|
| POST | `/account` | `AccountRequestDTO` | 201, `AccountResponseDTO`; 503 se não houver número de conta livre |
| GET | `/account/{id}` | — | 200, `AccountResponseDTO` (listas de transações vêm nulas) ⚠️ |
| GET | `/account/number/{number}` | — | 200, `AccountResponseDTO`; 404 se a conta não existir |
| GET | `/account/{id}/statement` | Query: `from`, `to` (datas `yyyy-MM-dd`, opcionais), `page` (começa em 0), `size` (padrão 20, máximo 100) | 200, página de `TransactionResponseDTO`; 400 se `from` for depois de `to`, a data tiver formato inválido, `page` for negativo ou `size` for menor que 1; 404 se a conta não existir |
| DELETE | `/account/{id}` | — | 200, sem corpo ⚠️; 409 se a conta tiver transações |

Depósito, saque e transferência são feitos somente por `POST /transaction`.

## Transações — `/transaction` (`TransactionController`)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/transaction` | `TransactionRequestDTO` | 201, `TransactionResponseDTO` |
| GET | `/transaction` | — | 200, lista de `TransactionResponseDTO` |
| GET | `/transaction/{id}` | — | 200, `TransactionResponseDTO`; 404 se a transação não existir |

### Extrato — `GET /account/{id}/statement`

Lista as transações em que a conta é origem **ou** destino (depósitos, saques e transferências nos dois sentidos), da mais recente para a mais antiga. A ordem é fixa: o parâmetro `sort` é ignorado. `from` e `to` são inclusivos e opcionais: `from=2026-03-01&to=2026-03-31` pega do primeiro instante de 1º de março ao último de 31 de março; só `from` ou só `to` filtra um lado.

```json
{
  "content": [
    { "id": 12, "amount": 50.00, "timestamp": "2026-03-20T08:30:00", "type": "TRANSFER", "senderId": 1, "receiverId": 2 }
  ],
  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
}
```

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
