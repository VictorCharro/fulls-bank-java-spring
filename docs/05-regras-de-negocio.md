# 05 — Regras de negócio

Código: `MAIN/services`. Erros lançados: [06](06-erros-e-excecoes.md).

## Cliente (`ClientService`)

- CPF deve ter 11 caracteres (apenas tamanho, sem validar dígitos).
- `name` e `lastName` não podem ser vazios.
- CPF duplicado lança `ExistingAccount`.
- Atualização altera somente `name` e `lastName`; o CPF nunca é alterado.
- Buscar, atualizar ou excluir cliente inexistente lança `ClientNotFound`.

## Conta (`AccountService`)

- Só cria conta para cliente existente (`ClientNotFound` caso contrário).
- Nova conta começa com saldo `0`.
- O número da conta é gerado aleatoriamente entre **1000 e 9999**, sem repetir.
- Busca por número inexistente lança `AccountNotFound`; busca ou exclusão por id inexistente lança `IdNotFound`.
- `deposit` e `withdrawal` rejeitam valor nulo, zero ou negativo com `NegativeAmount` (400).
- `withdrawal` com saldo menor que o valor lança `InsufficientBalance`.

## Transação (`TransactionService.createTransaction`)

Executa em uma única transação de banco (`@Transactional`).

Regra geral: `amount` nulo, zero ou negativo é rejeitado já na validação do DTO (`@NotNull` e `@Positive`), com 400. Chamado direto, o service ainda lança `IllegalArgumentException` (issue #4).

| Tipo | Campos usados | Regras | Efeito |
|---|---|---|---|
| `DEPOSIT` | `receiverId` | conta destino deve existir | soma ao saldo do destino |
| `WITHDRAWAL` | `senderId` | conta origem deve existir; saldo suficiente | subtrai do saldo da origem |
| `TRANSFER` | `senderId`, `receiverId` | ambas existem; saldo suficiente; contas diferentes | subtrai da origem e soma ao destino |

Em seguida é gravado um registro `Transaction` com tipo, valor, contas envolvidas e `timestamp` atual.

Observações:

- Em `TRANSFER`, a checagem de saldo vem **antes** da checagem de contas iguais.
- Contas inexistentes lançam `AccountNotFound`.
- Se qualquer regra falhar, nenhum saldo é alterado e nenhuma transação é gravada.
