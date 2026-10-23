# 05 — Regras de negócio

Código: `MAIN/services`. Erros lançados: [06](06-erros-e-excecoes.md).

## Cliente (`ClientService`)

- CPF deve ter 11 caracteres (apenas tamanho, sem validar dígitos).
- `name` e `lastName` não podem ser vazios.
- CPF duplicado lança `ExistingAccount`.
- Atualização altera somente `name` e `lastName`; o CPF nunca é alterado.
- Buscar, atualizar ou excluir cliente inexistente lança `ClientNotFound`.
- Excluir cliente que possui contas lança `ResourceInUse` (409); o cliente só pode ser excluído depois das contas. Se uma conta for criada entre a checagem e a exclusão, a violação de chave estrangeira também vira `ResourceInUse`.

## Conta (`AccountService`)

- Só cria conta para cliente existente (`ClientNotFound` caso contrário).
- Nova conta começa com saldo `0`.
- O número da conta é gerado aleatoriamente com **6 dígitos** (100000 a 999999), sem repetir. Após 20 tentativas sem achar um número livre, lança `AccountNumberUnavailable` (503).
- Busca por número inexistente lança `AccountNotFound`; busca ou exclusão por id inexistente lança `IdNotFound`.
- Excluir conta que participa de alguma transação (como origem ou destino) lança `ResourceInUse` (409).
- O `AccountService` não altera saldo: depósito, saque e transferência ficam só no `TransactionService`.

## Transação (`TransactionService.createTransaction`)

Executa em uma única transação de banco (`@Transactional` do Spring, `org.springframework.transaction.annotation`); qualquer exceção não verificada desfaz tudo.

Regra geral: `amount` nulo, zero ou negativo é rejeitado já na validação do DTO (`@NotNull` e `@Positive`), com 400. Chamado direto, o service lança `InvalidAmount` (400).

| Tipo | Campos usados | Regras | Efeito |
|---|---|---|---|
| `DEPOSIT` | `receiverId` | conta destino deve existir | soma ao saldo do destino |
| `WITHDRAWAL` | `senderId` | conta origem deve existir; saldo suficiente | subtrai do saldo da origem |
| `TRANSFER` | `senderId`, `receiverId` | ambas existem; saldo suficiente; contas diferentes (`SameAccountTransfer`, 400) | subtrai da origem e soma ao destino |

Em seguida é gravado um registro `Transaction` com tipo, valor, contas envolvidas e `timestamp` atual.

Observações:

- Em `TRANSFER`, a checagem de saldo vem **antes** da checagem de contas iguais.
- Contas inexistentes lançam `AccountNotFound`.
- Buscar transação por id inexistente lança `TransactionNotFound` (404).
- Se qualquer regra falhar, nenhum saldo é alterado e nenhuma transação é gravada.
- Concorrência: `Account` tem `@Version` (lock otimista). Se outra operação alterar a mesma conta entre a leitura e a gravação, a transação inteira é desfeita e a API responde `409`; o cliente deve repetir a requisição.
