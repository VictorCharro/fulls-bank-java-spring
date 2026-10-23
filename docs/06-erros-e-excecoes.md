# 06 — Erros e exceções

Código: `MAIN/exceptions`. Tratamento central em `GlobalExceptionHandler` (`@RestControllerAdvice`).

## Formato da resposta de erro

```json
{
  "status": 404,
  "message": "Account not found",
  "timestamp": "2026-10-22T14:30:00.123"
}
```

## Mapeamento

| Exceção | Status | Quando |
|---|---|---|
| `ClientNotFound` | 404 | Cliente não encontrado |
| `AccountNotFound` | 404 | Conta não encontrada (por número ou em transação) |
| `IdNotFound` | 404 | Conta não encontrada por id (`GET` e `DELETE /account/{id}`) |
| `ExistingAccount` | 400 | CPF já cadastrado |
| `TransactionNotFound` | 404 | Transação não encontrada por id |
| `AccountNumberUnavailable` | 503 | Não foi possível gerar um número de conta livre após 20 tentativas |
| `ResourceInUse` | 409 | Exclusão de cliente com contas ou de conta com transações |
| `ConcurrencyFailureException` | 409 | Conta alterada por outra operação ao mesmo tempo (lock otimista `@Version`) ou deadlock entre transferências em sentidos opostos; a transação é desfeita e o cliente pode repetir a requisição |
| `EmailAlreadyRegistered` | 400 | E-mail já cadastrado em `POST /auth/register` |
| `InvalidCredentials` | 401 | E-mail ou senha incorretos em `POST /auth/login` |
| `InsufficientBalance` | 400 | Saldo insuficiente |
| `InvalidPeriod` | 400 | Extrato com `from` depois de `to` |
| `InvalidPagination` | 400 | Extrato com `page` negativo ou `size` menor que 1 |
| `InvalidAmount` | 400 | `amount` nulo, zero ou negativo em `TransactionService.createTransaction` |
| `SameAccountTransfer` | 400 | Transferência com origem e destino iguais |
| `MethodArgumentNotValidException` | 400 | Falha de `@Valid`; mensagem no formato `campo: mensagem`, separadas por vírgula |

### Erros de segurança (filtros do Spring Security)

`401` (sem token, token inválido ou expirado) e `403` (papel sem permissão) não passam pelo `GlobalExceptionHandler`; `RestSecurityErrorHandler` os escreve no mesmo formato (`status`, `message`, `timestamp`).

## Como adicionar uma nova exceção

1. Criar a classe em `MAIN/exceptions` estendendo `RuntimeException`.
2. Adicionar um `@ExceptionHandler` em `GlobalExceptionHandler` com o status correto.
3. Atualizar a tabela acima e o doc [05](05-regras-de-negocio.md).
4. Cobrir com teste ([08](08-testes.md)).
